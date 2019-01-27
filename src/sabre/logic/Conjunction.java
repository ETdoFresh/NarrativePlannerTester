package sabre.logic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.function.Function;

import sabre.MutableState;
import sabre.Settings;
import sabre.State;
import sabre.util.ImmutableArray;

public class Conjunction extends BooleanExpression {

	private static final long serialVersionUID = Settings.VERSION_UID;

	public Conjunction(ImmutableArray<? extends Expression> arguments) {
		super(arguments);
	}
	
	public Conjunction(Iterable<? extends Expression> arguments) {
		this(new ImmutableArray<Expression>(arguments, Expression.class));
	}
	
	public Conjunction(Expression...arguments) {
		this(new ImmutableArray<>(arguments));
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Conjunction)
			return arguments.equals(((Conjunction) other).arguments);
		return false;
	}
	
	@Override
	public String toString() {
		return toString("&");
	}
	
	@Override
	public Conjunction apply(Function<Object, Object> function) {
		ImmutableArray<Expression> applied = applyArguments(function);
		if(applied == arguments)
			return this;
		else
			return new Conjunction(applyArguments(function));
	}
	
	@Override
	public boolean isTestable() {
		return true;
	}
	
	@Override
	public boolean test(State state) {
		for(int i=0; i<arguments.size(); i++)
			if(!arguments.get(i).test(state))
				return false;
		return true;
	}
	
	@Override
	public boolean isImposable() {
		return true;
	}
	
	@Override
	public boolean impose(State previous, MutableState state) {
		boolean result = false;
		for(int i=0; i<arguments.size(); i++)
			result = arguments.get(i).impose(previous, state) || result;
		return result;
	}
	
	@Override
	public Expression simplify() {
		LinkedHashSet<Expression> simplified = new LinkedHashSet<>();
		boolean different = false;
		for(Expression argument : arguments) {
			Expression s = argument.simplify();
			if(argument != s)
				different = true;
			if(s == Expression.TRUE)
				continue;
			else if(s == Expression.FALSE)
				return Expression.FALSE;
			else
				simplified.add(s);
		}
		if(simplified.size() == 0)
			return Expression.TRUE;
		else if(simplified.size() == 1)
			return simplified.iterator().next();
		else if(different || simplified.size() != arguments.size())
			return new Conjunction(new ImmutableArray<Expression>(simplified, Expression.class));
		else
			return this;
	}
	
	@Override
	public Disjunction negate() {
		return new Disjunction(negateArguments());
	}

	@Override
	public DNFExpression toDNF() {
		DNFExpression[] arguments = new DNFExpression[this.arguments.size()];
		for(int i=0; i<arguments.length; i++)
			arguments[i] = this.arguments.get(i).toDNF();
		ArrayList<ConjunctiveClause> clauses = new ArrayList<>();
		collectClauses(arguments, 0, new ConjunctiveClause[arguments.length], clauses);
		Expression expression = new DNFExpression(clauses).simplify();
		if(expression instanceof DNFExpression)
			return (DNFExpression) expression;
		else
			return expression.toDNF();
	}
	
	private static final void collectClauses(DNFExpression[] arguments, int index, ConjunctiveClause[] parts, ArrayList<ConjunctiveClause> clauses) {
		if(index == arguments.length) {
			ArrayList<Literal> literals = new ArrayList<>();
			for(ConjunctiveClause part : parts)
				for(Literal literal : part.arguments)
					literals.add(literal);
			clauses.add(new ConjunctiveClause(literals));
		}
		else {
			for(ConjunctiveClause part : arguments[index].arguments) {
				parts[index] = part;
				collectClauses(arguments, index + 1, parts, clauses);
			}
		}
	}
}
