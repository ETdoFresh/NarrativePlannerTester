package sabre.io;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import sabre.*;
import sabre.logic.*;
import sabre.logic.Comparison.Operator;
import sabre.util.ImmutableArray;
import sabre.util.ImmutableList;
import sabre.util.ImmutableSet;

public class DefaultParser extends Parser {

	private static final NonTerminal TYPE_DEFINITION = new NonTerminal("type definition");

	private static final Builder TYPE_DEFINITION_BUILDER = new Builder() {

		@Override
		public Object build(ParseTree tree) throws ParseException {
			DomainBuilder builder = getDomainBuilder(tree);
			String name = tree.child(0).build(String.class);
			builder.addType(name, tree.getComment());
			for(ParseTree child : tree.child(1))
				builder.addTypeRelationship(child.build(String.class), name);
			return null;
		}
	};
	
	private static final NonTerminal ENTITY_DEFINITION = new NonTerminal("entity definition");
	
	private static final Builder ENTITY_DEFINITION_BUILDER = new Builder() {

		@Override
		public Object build(ParseTree tree) throws ParseException {
			DomainBuilder builder = getDomainBuilder(tree);
			String type = tree.child(0).build(String.class);
			String name = tree.child(1).build(String.class);
			builder.addEntity(name, tree.getComment());
			builder.addEntityRelationship(type, name);
			return null;
		}
	};
	
	private static final NonTerminal PROPERTY_DEFINITION = new NonTerminal("property definition");
	
	private static final Builder PROPERTY_DEFINITION_BUILDER = new Builder() {

		@Override
		public Object build(ParseTree tree) throws ParseException {
			DomainBuilder builder = getDomainBuilder(tree);
			String type = tree.child(0).build(String.class);
			String name = tree.child(1).build(String.class);
			ArrayList<String> parameters = new ArrayList<>();
			for(ParseTree child : tree.child(2))
				parameters.add(child.build(String.class));
			String defaultValue = tree.child(3).build(String.class);
			builder.addProperty(name, tree.getComment(), type, parameters, defaultValue);
			return null;
		}
	};
	
	private static abstract class EventDefinitionBuilder<E extends Event> implements Builder {

		@Override
		public Event build(ParseTree tree) throws ParseException {
			DomainBuilder builder = getDomainBuilder(tree);
			String name = tree.child(0).build(String.class);
			ArrayList<Logical> arguments = new ArrayList<>();
			LinkedHashSet<Term> agents = new LinkedHashSet<>();
			for(ParseTree child : tree.child(1)) {
				Type ptype = child.child(0).build(Type.class);
				String pname = child.child(1).build(String.class);
				TermVariable parameter = new TermVariable(pname, ptype);
				arguments.add(parameter);
				tree.define(parameter);
				if(child.tokens.size() == 3)
					agents.add(parameter);
			}
			Expression precondition = tree.child(2).build(Expression.class);
			Expression effect = tree.child(3).build(Expression.class);
			return build(tree, builder, name, tree.getComment(), new ImmutableArray<>(arguments, Logical.class), precondition, effect, new ImmutableSet<>(agents, Term.class));
		}
		
		protected abstract E build(ParseTree tree, DomainBuilder builder, String name, String comment, ImmutableArray<Logical> arguments, Expression precondition, Expression effect, ImmutableSet<Term> agents) throws ParseException;
	};
	
	private static final Builder ACTION_DEFINITION_BUILDER = new EventDefinitionBuilder<Action>() {

		@Override
		protected Action build(ParseTree tree, DomainBuilder builder, String name, String comment, ImmutableArray<Logical> arguments, Expression precondition, Expression effect, ImmutableSet<Term> agents) throws ParseException {
			Action action = new Action(name, comment, arguments, precondition, effect, agents);
			builder.addAction(action);
			return action;
		}
	};
	
	private static final Builder AXIOM_DEFINITION_BUILDER = new EventDefinitionBuilder<Axiom>() {

		@Override
		protected Axiom build(ParseTree tree, DomainBuilder builder, String name, String comment, ImmutableArray<Logical> arguments, Expression precondition, Expression effect, ImmutableSet<Term> agents) throws ParseException {
			if(agents.size() != 0)
				throw new ParseException("Axioms cannot have consenting agents.", tree.tokens);
			Axiom axiom = new Axiom(name, comment, arguments, precondition, effect);
			builder.addAxiom(axiom);
			return axiom;
		}
	};
	
	private static final Builder TYPE_BUILDER = new Builder() {

		@Override
		public Type build(ParseTree tree) throws ParseException {
			String name = tree.child(0).build(String.class);
			return getDomain(tree).getType(name);
		}
	};
	
	private static abstract class BooleanExpressionBuilder implements Builder {
		
		@Override
		public Object build(ParseTree tree) throws ParseException {
			ArrayList<Expression> arguments = new ArrayList<>();
			for(ParseTree child : tree.child(0))
				arguments.add(child.build(Expression.class));
			return build(arguments.toArray(new Expression[arguments.size()]));
		}
		
		protected abstract BooleanExpression build(Expression[] arguments) throws ParseException;
	}
	
	private static final Builder CONJUNCTION_BUILDER = new BooleanExpressionBuilder() {

		@Override
		protected Conjunction build(Expression[] arguments) throws ParseException {
			return new Conjunction(arguments);
		}
	};
	
	private static final Builder DISJUNCTION_BUILDER = new BooleanExpressionBuilder() {

		@Override
		protected Disjunction build(Expression[] arguments) throws ParseException {
			return new Disjunction(arguments);
		}
	};
	
	private static final Builder NEGATION_BUILDER = new Builder() {

		@Override
		public Negation build(ParseTree tree) throws ParseException {
			if(tree.child(0).type == NegatedLiteral.class)
				return tree.child(0).build(Negation.class);
			else {
				Expression argument = tree.child(0).build(Expression.class);
				if(argument instanceof Literal)
					return new NegatedLiteral((Literal) argument);
				else
					return new Negation(argument);
			}
		}
	};
	
	private static abstract class QuantifiedExpressionBuilder implements Builder {
		
		@Override
		public Object build(ParseTree tree) throws ParseException {
			Type type = tree.child(0).build(Type.class);
			String name = tree.child(1).build(String.class);
			TermVariable variable = new TermVariable(name, type);
			tree.define(variable);
			Expression argument = tree.child(2).build(Expression.class);
			return build(variable, argument);
		}
		
		protected abstract QuantifiedExpression build(TermVariable variable, Expression argument);
	}
	
	private static final Builder UNIVERSAL_QUANTIFICATION_BUILDER = new QuantifiedExpressionBuilder() {

		@Override
		protected UniversalQuantification build(TermVariable variable, Expression argument) {
			return new UniversalQuantification(variable, argument);
		}
	};
	
	private static final Builder EXISTENTIAL_QUANTIFICATION_BUILDER = new QuantifiedExpressionBuilder() {

		@Override
		protected ExistentialQuantification build(TermVariable variable, Expression argument) {
			return new ExistentialQuantification(variable, argument);
		}
	};
	
	private static final NonTerminal NEGATED_ASSIGNMENT = new NonTerminal("negated assignment");
	
	private static final Builder NEGATED_ASSIGNMENT_BUILDER = new Builder() {

		@Override
		public NegatedLiteral build(ParseTree tree) throws ParseException {
			return new NegatedLiteral((Assignment) ASSIGNMENT_BUILDER.build(tree));
		}
	};
	
	private static final Builder COMPARISON_BUILDER = new Builder() {
		
		@Override
		public Object build(ParseTree tree) throws ParseException {
			ParseTree leftTree = tree.child(0).child(0);
			ParseTree rightTree = tree.child(0).child(1);
			ImmutableList<Token> link = tree.tokens;
			for(int i=0; i<leftTree.tokens.size(); i++)
				link = link.rest;
			Operator operator;
			if(link.first.content.equals("=="))
				operator = Comparison.EQUALS;
			else if(link.first.content.equals("!="))
				operator = Comparison.NOT_EQUALS;
			else
				throw new ParseException("Comparison \"" + link.first.content + "\" not recognized.", Token.first(link));
			Logical left = leftTree.build(Logical.class);
			Logical right = rightTree.build(Logical.class);
			return new Comparison(operator, left, right);
		}
	};
	
	private static final Builder ASSIGNMENT_BUILDER = new Builder() {
		
		@Override
		public Assignment build(ParseTree tree) throws ParseException {
			Domain domain = getDomain(tree);
			String name = tree.child(0).build(String.class);
			ArrayList<Logical> arguments = new ArrayList<>();
			for(ParseTree child : tree.child(1))
				arguments.add(child.build(Logical.class));
			Term value = tree.child(2).build(Term.class);
			if(value == null)
				value = Utilities.getTrue(domain);
			Assignment result = null;
			FormatException exception = null;
			for(Property property : domain.properties) {
				try {
					if(property.name.equals(name))
						result = new Assignment(property, arguments, value);
				}
				catch(FormatException ex) {
					exception = ex;
				}
			}
			if(result != null)
				return result;
			else if(exception == null)
				throw new ParseException("Property \"" + name + "\" not defined.", tree.tokens);
			else
				throw exception;
		}
	};
	
	private static final Builder TERM_BUILDER = new Builder() {

		@Override
		public Object build(ParseTree tree) throws ParseException {
			String name = tree.child(0).build(String.class);
			Term term = tree.lookup(TermVariable.class, tv -> tv.name.equals(name));
			if(term == null)
				term = getDomain(tree).getEntity(name);
			if(term == null)
				throw new ParseException("Term \"" + name + "\" not defined.", tree.tokens);
			return term;
		}
	};
	
	private static final Builder ENTITY_BUILDER = new Builder() {

		@Override
		public Entity build(ParseTree tree) throws ParseException {
			String name = tree.child(0).build(String.class);
			return getDomain(tree).getEntity(name);
		}
	};
	
	private static final Builder TERM_VARIABLE_BUILDER = new Builder() {

		@Override
		public TermVariable build(ParseTree tree) throws ParseException {
			String name = tree.child(0).build(String.class);
			TermVariable variable = tree.lookup(TermVariable.class, t -> t.name.equals(name));
			if(variable == null)
				throw new ParseException("Variable \"" + name + "\" not defined.", tree.tokens);
			else
				return variable;
		}
	};
	
	private static final NonTerminal STANDALONE_EXPRESSION = new NonTerminal("initial state expression");
	
	private static final Builder STANDALONE_EXPRESSION_BUILDER = new Builder() {

		@Override
		public Object build(ParseTree tree) throws ParseException {
			DomainBuilder builder = getDomainBuilder(tree);
			Expression expression = tree.child(0).build(Expression.class);
			builder.addToInitialState(expression);
			return expression;
		}
	};
	
	private static final NonTerminal GOAL_DEFINITION = new NonTerminal("goal");
	
	private static final Builder GOAL_BUILDER = new Builder() {

		@Override
		public Object build(ParseTree tree) throws ParseException {
			DomainBuilder builder = getDomainBuilder(tree);
			builder.setGoal(tree.child(0).build(Expression.class));
			return null;
		}
	};
	
	private static final Builder DOMAIN_BUILDER = new Builder() {

		@Override
		public Domain build(ParseTree tree) throws ParseException {
			// Create domain builder.
			String name = tree.child(0).build(String.class);
			DomainBuilder builder = new DomainBuilder(new Domain(name, tree.getComment()));
			tree.define(builder);
			// Group definitions.
			ArrayList<ParseTree> types = new ArrayList<>();
			ArrayList<ParseTree> entities = new ArrayList<>();
			ArrayList<ParseTree> properties = new ArrayList<>();
			ArrayList<ParseTree> other = new ArrayList<>();
			for(ParseTree child : tree.child(1)) {
				if(child.child(0).type == TYPE_DEFINITION.key)
					types.add(child);
				else if(child.child(0).type == ENTITY_DEFINITION.key)
					entities.add(child);
				else if(child.child(0).type == PROPERTY_DEFINITION.key)
					properties.add(child);
				else
					other.add(child);
			}
			// Define all type and entity symbols.
			for(ParseTree child : types)
				builder.addType(child.child(0).child(0).build(String.class), child.getComment());
			for(ParseTree child : entities)
				builder.addEntity(child.child(0).child(1).build(String.class), child.getComment());
			// Define type and entity relationships.
			for(ParseTree child : types)
				child.build();
			for(ParseTree child : entities)
				child.build();
			// Define properties.
			for(ParseTree child : properties)
				child.build();
			// Define everything else.
			tree.define(builder.getDomain());
			for(ParseTree child : other)
				child.build();
			// Create domain.
			return builder.getDomain();
		}
	};
	
	private static final Domain getDomain(ParseTree tree) throws ParseException {
		Domain domain = tree.lookup(Domain.class);
		if(domain == null)
			throw new ParseException("No domain defined.", tree.tokens);
		else
			return domain;
	}
	
	private static final DomainBuilder getDomainBuilder(ParseTree tree) throws ParseException {
		DomainBuilder builder = tree.lookup(DomainBuilder.class);
		if(builder == null)
			throw new ParseException("No domain defined.", tree.tokens);
		else
			return builder;
	}
	
	public DefaultParser() {
		// Ignore the contents of parentheses during lookahead.
		setBrackets("(", ")");
		
		// Type Definition
		addRule(TYPE_DEFINITION,
			"type", Pattern.SYMBOL, "extends", new List(Pattern.SYMBOL, ","), ";");
		addRule(TYPE_DEFINITION,
			"type", Pattern.SYMBOL, Pattern.NOTHING, ";");
		setBuilder(TYPE_DEFINITION, TYPE_DEFINITION_BUILDER);
		// Entity Definition
		addRule(ENTITY_DEFINITION,
			Pattern.SYMBOL, ":", Pattern.SYMBOL, ";");
		setBuilder(ENTITY_DEFINITION, ENTITY_DEFINITION_BUILDER);
		// Property Definition
		addRule(PROPERTY_DEFINITION,
			Pattern.SYMBOL, ":", Pattern.SYMBOL, "(", new List(Pattern.SYMBOL, ","), ")", "=", Pattern.SYMBOL, ";");
		addRule(PROPERTY_DEFINITION,
			Pattern.SYMBOL, ":", Pattern.SYMBOL, "(", new List(Pattern.SYMBOL, ","), ")", Pattern.NOTHING, ";");
		setBuilder(PROPERTY_DEFINITION, PROPERTY_DEFINITION_BUILDER);
		// Parameter
		NonTerminal parameter = new NonTerminal("parameter");
		addRule(parameter,
			Type.class, Pattern.SYMBOL, "*");
		addRule(parameter,
			Type.class, Pattern.SYMBOL);
		// Event Definitions
		NonTerminal eventDefinition = new NonTerminal("event definition");
		NonTerminal actionDefinition = new NonTerminal("action definition");
		NonTerminal axiomDefinition = new NonTerminal("axiom definition");
		addRule(eventDefinition, actionDefinition);
		addRule(actionDefinition,
			"action", Pattern.SYMBOL, "(", new List(parameter, ","), ")", "{", "precondition", ":", Expression.class, ";", "effect", ":", Expression.class, ";", "}");
		setBuilder(actionDefinition, ACTION_DEFINITION_BUILDER);
		addRule(eventDefinition, axiomDefinition);
		addRule(axiomDefinition,
			"axiom", Pattern.SYMBOL, "(", new List(parameter, ","), ")", "{", "precondition", ":", Expression.class, ";", "effect", ":", Expression.class, ";", "}");
		setBuilder(axiomDefinition, AXIOM_DEFINITION_BUILDER);
		// Type
		addRule(Type.class, Pattern.SYMBOL);
		setBuilder(Type.class, TYPE_BUILDER);
		// Logical
		addRule(Logical.class, Expression.class);
		addRule(Logical.class, Term.class);
		// Expression
		addRule(Expression.class,
			"(", Expression.class, ")");
		addRule(Expression.class, BooleanExpression.class);
		addRule(Expression.class, QuantifiedExpression.class);
		addRule(Expression.class, Literal.class);
		// Boolean Expression
		addRule(BooleanExpression.class, Conjunction.class);
		addRule(BooleanExpression.class, Disjunction.class);
		addRule(BooleanExpression.class, Negation.class);
		// Conjunction
		addRule(Conjunction.class,
			new List(Expression.class, "&", 2));
		setBuilder(Conjunction.class, CONJUNCTION_BUILDER);
		// Disjunction
		addRule(Disjunction.class,
			new List(Expression.class, "|", 2));
		setBuilder(Disjunction.class, DISJUNCTION_BUILDER);
		// Negation
		addRule(Negation.class, NegatedLiteral.class);
		addRule(Negation.class,
			"!", Expression.class);
		setBuilder(Negation.class, NEGATION_BUILDER);
		// Quantified Expression
		addRule(QuantifiedExpression.class, UniversalQuantification.class);
		addRule(QuantifiedExpression.class, ExistentialQuantification.class);
		// Universal Quantification
		addRule(UniversalQuantification.class,
			"forall", "(", Type.class, Pattern.SYMBOL, ")", Expression.class);
		setBuilder(UniversalQuantification.class, UNIVERSAL_QUANTIFICATION_BUILDER);
		// Existential Quantification
		addRule(ExistentialQuantification.class,
			"exists", "(", Type.class, Pattern.SYMBOL, ")", Expression.class);
		setBuilder(ExistentialQuantification.class, EXISTENTIAL_QUANTIFICATION_BUILDER);
		// Literal
		addRule(Literal.class, NegatedLiteral.class);
		addRule(Literal.class, Comparison.class);
		addRule(Literal.class, Assignment.class);
		// Negated Literal
		addRule(NegatedLiteral.class, NEGATED_ASSIGNMENT);
		addRule(NEGATED_ASSIGNMENT,
			Pattern.SYMBOL, "(", new List(Logical.class, ","), ")", "!=", Term.class);
		setBuilder(NEGATED_ASSIGNMENT, NEGATED_ASSIGNMENT_BUILDER);
		// Comparisons
		addRule(Comparison.class,
			new List(Term.class, "==", 2, 2));
		addRule(Comparison.class,
			new List(Expression.class, "==", 2, 2));
		addRule(Comparison.class,
			new List(Term.class, "!=", 2, 2));
		addRule(Comparison.class,
			new List(Expression.class, "!=", 2, 2));
		setBuilder(Comparison.class, COMPARISON_BUILDER);
		// Assignment
		addRule(Assignment.class,
			Pattern.SYMBOL, "(", new List(Logical.class, ","), ")", "=", Term.class);
		addRule(Assignment.class,
			Pattern.SYMBOL, "(", new List(Logical.class, ","), ")", Pattern.NOTHING);
		setBuilder(Assignment.class, ASSIGNMENT_BUILDER);
		// Terms
		addRule(Term.class, Pattern.SYMBOL);
		setBuilder(Term.class, TERM_BUILDER);
		addRule(Term.class, TermVariable.class);
		addRule(Term.class, Entity.class);
		// Term Variable
		addRule(TermVariable.class, Pattern.SYMBOL);
		setBuilder(TermVariable.class, TERM_VARIABLE_BUILDER);
		// Entity
		addRule(Entity.class, Pattern.SYMBOL);
		setBuilder(Entity.class, ENTITY_BUILDER);
		// Standalone Expression
		addRule(STANDALONE_EXPRESSION,
			Expression.class, ";");
		setBuilder(STANDALONE_EXPRESSION, STANDALONE_EXPRESSION_BUILDER);
		// Goal Definition (must come before property definition)
		addRule(GOAL_DEFINITION, "goal", ":", Expression.class, ";");
		setBuilder(GOAL_DEFINITION, GOAL_BUILDER);		
		
		// Domain
		NonTerminal definition = new NonTerminal("definition");
		addRule(definition, LANDMARK_DEFINITION); // needs to come before property definition because of similar syntax
		addRule(definition, GOAL_DEFINITION); // needs to come before property definition because of similar syntax
		addRule(definition, TYPE_DEFINITION);
		addRule(definition, ENTITY_DEFINITION);
		addRule(definition, PROPERTY_DEFINITION);
		addRule(definition, eventDefinition);
		addRule(definition, STANDALONE_EXPRESSION);
		addRule(Domain.class,
			"domain", ":", Pattern.STRING, ";", new List(definition, ""));
		setBuilder(Domain.class, DOMAIN_BUILDER);
		
		addRule(LANDMARK_DEFINITION, "landmark", ":", Pattern.STRING, ";");
		//addRule(LANDMARK_DEFINITION, "landmark", ":", Pattern.SYMBOL, "(", new List(parameter, ","), ")");
		setBuilder(LANDMARK_DEFINITION, LANDMARK_BUILDER);
	}
	
	private static final NonTerminal LANDMARK_DEFINITION = new NonTerminal("landmark");
	private static final Builder LANDMARK_BUILDER = new Builder() {

		@Override
		public Object build(ParseTree tree) throws ParseException {
			DomainBuilder builder = getDomainBuilder(tree);
			builder.setLandmark(tree.child(0).build(String.class));
			return null;
		}
	};
}
