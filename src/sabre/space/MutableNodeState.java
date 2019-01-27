package sabre.space;

import java.io.Serializable;

import sabre.Agent;
import sabre.Axiom;
import sabre.Entity;
import sabre.Event;
import sabre.MutableState;
import sabre.Settings;
import sabre.logic.Expression;

final class MutableNodeState implements MutableState, Serializable {
	
	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public Node before = null;
	public Event event = null;
	public Node after = null;
		
	@Override
	public SearchSpace getSearchSpace() {
		return before.getSearchSpace();
	}

	@Override
	public Entity get(Slot slot) {
		if(after == null)
			return before.get(slot);
		else
			return after.get(slot);
	}

	@Override
	public boolean set(Slot slot, Entity value) {
		if(before.get(slot) != value) {
			if(after == null)
				after = new AxiomNode(before, (Axiom) event);
			if(slot.property.id == Settings.INTENTIONAL_PROPERTY_ID) {
				Agent agent = (Agent) slot.arguments.get(0);
				Expression goal = (Expression) slot.arguments.get(1);
				if(value.id == Settings.BOOLEAN_FALSE_ID)
					after.goals = after.goals.removeIf(e -> e.agent == agent && e.goal == goal);
				else {
					
					after.goals = after.goals.add(new Explanation(slot, after));
				}
			}
			return true;
		}
		else
			return false;
	}
}
