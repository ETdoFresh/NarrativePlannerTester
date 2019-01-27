package sabre;

import java.io.StringWriter;
import java.util.ArrayList;

import sabre.logic.Conjunction;
import sabre.logic.Expression;
import sabre.logic.Literal;
import sabre.logic.SlotAssignment;
import sabre.space.SearchSpace;
import sabre.space.Slot;

public interface State {
	
	public static boolean equals(State state, Object other) {
		if(other instanceof State) {
			State otherState = (State) other;
			SearchSpace space = state.getSearchSpace();
			if(space != otherState.getSearchSpace())
				return false;
			for(int i=0; i<space.slots.size(); i++) {
				Slot slot = space.slots.get(i);
				if(state.get(slot) != otherState.get(slot))
					return false;
			}
			return true;
		}
		return false;
	}
	
	public static int hashCode(State state) {
		SearchSpace space = state.getSearchSpace();
		int hc = 0;
		for(int i=0; i<space.slots.size(); i++)
			hc = hc * 31 + state.get(space.slots.get(i)).hashCode();
		return hc;
	}
	
	public static String toString(State state) {
		StringWriter string = new StringWriter();
		string.append("State: ");
		boolean first = true;
		for(Slot slot : state.getSearchSpace().slots) {
			if(first)
				first = false;
			else
				string.append(", ");
			string.append(slot.toString());
			string.append(" = ");
			string.append(state.get(slot).toString());
		}
		return string.toString();
	}

	public abstract SearchSpace getSearchSpace();
	
	public abstract Entity get(Slot slot);
	
	public default Expression toExpression() {
		ArrayList<Literal> literals = new ArrayList<>();
		for(Slot slot : getSearchSpace().slots)
			literals.add(new SlotAssignment(slot, get(slot)));
		return new Conjunction(literals).simplify();
	}
}
