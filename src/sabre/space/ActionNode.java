package sabre.space;

import sabre.Action;
import sabre.Agent;
import sabre.util.ImmutableList;

public class ActionNode extends Node {

	public final Action event;
	private boolean explained;
	
	ActionNode(Node parent, Action action) {
		super(parent, action);
		this.event = action;
		this.explained = action.agents.size() == 0;
		checkGoals();
	}

	@Override
	public boolean isExplained() {
		return explained;
	}
	
	@Override
	public boolean isExplained(Agent agent) {
		ImmutableList<IntentionalChain> current = explanations;
		while(current.size != 0) {
			if(current.first.explanation.agent == agent && current.first.contains(this))
				return true;
			current = current.rest;
		}
		return false;
	}
	
	@Override
	void propagate(IntentionalChain chain) {
		if(explained)
			super.propagate(chain);
		else {
			explanations = explanations.add(chain);
			if(checkExplanations()) {
				explained = true;
				ImmutableList<IntentionalChain> current = explanations;
				explanations = Node.NO_EXPLANATIONS;
				while(current.size != 0) {
					propagate(current.first);
					current = current.rest;
				}
			}
		}
	}
	
	private final boolean checkExplanations() {
		for(int i=0; i<event.agents.size(); i++)
			if(!isExplained((Agent) event.agents.get(i)))
				return false;
		return true;
	}
}
