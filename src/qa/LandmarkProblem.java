package qa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import sabre.Action;
import sabre.Axiom;
import sabre.Domain;
import sabre.Entity;
import sabre.Event;
import sabre.Property;
import sabre.Type;
import sabre.Utilities;
import sabre.logic.Assignment;
import sabre.logic.Conjunction;
import sabre.logic.ConjunctiveClause;
import sabre.logic.DNFExpression;
import sabre.logic.Disjunction;
import sabre.logic.Expression;
import sabre.logic.Literal;
import sabre.logic.Logical;
import sabre.logic.Term;
import sabre.logic.TermVariable;
import sabre.logic.Variable;
import sabre.space.Slot;
import sabre.util.ImmutableArray;

public class LandmarkProblem {
	
	//protected Assignment finalEffect = null;
	//protected Map<String, Expression> goals = new LinkedHashMap<>();
	
	protected ArrayList<Action> actions = new ArrayList<>();
	protected ArrayList<Axiom> axioms = new ArrayList<>();
	
	public Domain domain;
	
	public LandmarkProblem(Domain og_domain, Event[] landmarkEvents) {
		// For each event in the list, create a landmarked event and collect their new effects
		ArrayList<Literal> landmarkLiterals = new ArrayList<>();
		for(Event landmark : landmarkEvents) 
			landmarkLiterals.add(this.transform(landmark, og_domain));
		// Add all the effects to the author's goal
		ArrayList<ConjunctiveClause> newGoalClauses = new ArrayList<ConjunctiveClause>();
		for(ConjunctiveClause goalClause : og_domain.goal.toDNF().arguments) {
			ArrayList<Literal> literals = new ArrayList<>();
			literals.addAll(landmarkLiterals);
			for(Literal goalLiteral : goalClause.arguments)
				literals.add(goalLiteral);
			newGoalClauses.add(new ConjunctiveClause(literals));
		}
		// Clone the original domain but with the new goal
		domain = new Domain(og_domain, og_domain.name, og_domain.comment, new DNFExpression(newGoalClauses));
		// Now add all the new actions and axioms to the domain
		for(Action action : actions) 
			domain.addAction(action);
		for(Axiom axiom : axioms)
			domain.addAxiom(axiom);
	}
		
	/** Add an event to the problem based on the given observation */
	private Literal transform(Event event, Domain domain) {
		// New name = "p_" + old name 
		String newName = "p_" + event.name;
		// New effect = "event_name = True"
		
		/**
		 * TODO: The ID's might need to be calculated to be increasing?
		 * Not sure what else needs to be done. :/
		 * Halp.
		 */
		
		Type type = new Type(domain, 11, newName, "");
		Property newProperty = new Property(domain, Integer.MAX_VALUE, newName, "", type, new ImmutableArray<Variable>(new Variable[0]), new Entity(domain, Integer.MAX_VALUE, "False", ""));
		domain.addProperty(newProperty);
		Literal newEffect = new Assignment(newProperty, new ImmutableArray<Logical>(new Logical[0]), new TermVariable("True", type));
		ArrayList<Expression> effects = new ArrayList<>(); 
		for(ConjunctiveClause clause : event.effect.toDNF().arguments)
			effects.add(clause);
		effects.add(newEffect);
		// Effects = special effect + effects of observed event
		if(event instanceof Action)
			actions.add(new Action(newName, event.comment, event.arguments, event.precondition, new Conjunction(newEffect, new Conjunction(effects)), ((Action)event).agents));
		else
			axioms.add(new Axiom(newName, event.comment, event.arguments, event.precondition, new Conjunction(newEffect, new Conjunction(effects))));
		return newEffect;
	}
}
