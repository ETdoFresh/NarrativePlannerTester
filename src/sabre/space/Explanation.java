package sabre.space;

import sabre.Agent;
import sabre.logic.Expression;
import sabre.logic.Literal;

public class Explanation {

	public final Slot slot;
	public final Agent agent;
	public final Expression goal;
	public final Node motivation;
	public final Node satisfaction;
	
	private Explanation(Slot slot, Node motivation, Node satisfaction) {
		this.slot = slot;
		this.agent = (Agent) slot.arguments.get(0);
		this.goal = (Expression) slot.arguments.get(1);
		this.motivation = motivation;
		this.satisfaction = satisfaction;
	}
	
	Explanation(Slot slot, Node motivation) {
		this(slot, motivation, null);
	}
	
	Explanation(Explanation toClone, Node satisfaction) {
		this(toClone.slot, toClone.motivation, satisfaction);
	}
	
	@Override
	public String toString() {
		return agent + " achieves " + goal;
	}
	
	final IntentionalChain extend(Node tail, Literal link) {
		return Utilities.check(new IntentionalChain(tail, link, null, this));
	}
}
