package sabre.space;

import sabre.Entity;
import sabre.logic.Assignment;
import sabre.logic.Comparison;
import sabre.logic.Conjunction;
import sabre.logic.Disjunction;
import sabre.logic.Expression;
import sabre.logic.Logical;
import sabre.logic.NegatedLiteral;
import sabre.logic.Negation;
import sabre.logic.SlotAssignment;

final class NonDeletingState {

	private final SearchSpace space;
	
	public NonDeletingState(SearchSpace space) {
		this.space = space;
	}
	
	public boolean canBeTrue(Expression expression) {
		return canBe(true, expression);
	}
	
	private boolean canBe(boolean valence, Expression expression) {
		if(expression == Expression.TRUE)
			return valence;
		else if(expression == Expression.FALSE)
			return !valence;
		else if(expression instanceof Assignment) {
			Assignment assignment = (Assignment) expression;
			// If the assignment is not ground, it can be either true or false.
			if(!expression.isGround())
				return true;
			// The assignment can be true if it has been imposed.
			Slot slot = slot(assignment);
			if(valence)
				return space.tree.get(slot.property, slot.arguments, (Entity) assignment.value) != null;
			// The assignment can be false if has not been imposed of it other assignments to the same slot have been imposed.
			else
				return space.tree.get(slot.property, slot.arguments, (Entity) assignment.value) == null || space.tree.size(slot.property, slot.arguments) > 1;
		}
		else if(expression instanceof Negation)
			return canBe(!valence, ((Negation) expression).argument);
		else if(expression instanceof Comparison) {
			if(expression.isGround())
				return canBe(valence, expression.simplify());
			else
				return true;
		}
		else if(expression instanceof Conjunction) {
			for(Expression argument : ((Conjunction) expression).arguments)
				if(!canBe(valence, argument))
					return false;
			return true;
		}
		else if(expression instanceof Disjunction) {
			for(Expression argument : ((Disjunction) expression).arguments)
				if(canBe(valence, argument))
					return true;
			return false;
		}
		else
			return canBe(valence, expression.toDNF());
	}
	
	public void impose(Expression expression) {
		if(expression == Expression.TRUE || expression == Expression.FALSE)
			return;
		else if(expression instanceof Assignment) {
			Assignment assignment = (Assignment) expression;
			slot(assignment);
			space.tree.put(assignment);
		}
		else if(expression instanceof NegatedLiteral)
			impose(expression.negate().negate());
		else if(expression instanceof Conjunction)
			for(Expression argument : ((Conjunction) expression).arguments)
				impose(argument);
		else
			impose(expression.toDNF().arguments.get(0));
	}
	
	private Slot slot(Assignment assignment) {
		Slot slot = space.tree.get(assignment.property, assignment.arguments);
		if(slot == null) {
			for(Logical argument : assignment.arguments)
				if(argument instanceof Expression)
					canBeTrue((Expression) argument);
			slot = new Slot(space, space.tree.size(), assignment.property, assignment.arguments, assignment.property.defaultValue);
			space.tree.put(slot);
			space.tree.put(new SlotAssignment(slot, slot.initial));
		}
		return slot;
	}
}
