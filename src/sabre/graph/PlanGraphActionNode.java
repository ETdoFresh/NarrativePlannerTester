package sabre.graph;

import sabre.Action;
import sabre.Settings;

public class PlanGraphActionNode extends PlanGraphEventNode {
	
	private static final long serialVersionUID = Settings.VERSION_UID;

	public final Action event;
	
	PlanGraphActionNode(PlanGraph graph, Action action) {
		super(graph, action);
		this.event = action;
	}
	
	@Override
	protected boolean setSatisfied(int level) {
		if(super.setSatisfied(level)) {
			graph.pending.add(this);
			return true;
		}
		else
			return false;
	}	
}
