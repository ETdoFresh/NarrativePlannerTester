package sabre.graph;

import sabre.Settings;
import sabre.logic.ConjunctiveClause;
import sabre.logic.DNFExpression;
import sabre.util.ImmutableSet;
import sabre.util.MutableSet;

public class PlanGraphSatisfiableNode extends PlanGraphNode {
	
	private static final long serialVersionUID = Settings.VERSION_UID;

	public final ImmutableSet<PlanGraphClauseNode> parents = new MutableSet<>(new PlanGraphClauseNode[0]);
	private int key = -1;
	
	PlanGraphSatisfiableNode(PlanGraph graph, DNFExpression precondition) {
		super(graph);
		for(ConjunctiveClause clause : precondition.arguments) {
			PlanGraphClauseNode parent = new PlanGraphClauseNode(graph, clause);
			((MutableSet<PlanGraphClauseNode>) parents).add(parent);
			((MutableSet<PlanGraphSatisfiableNode>) parent.children).add(this);
		}
	}
	
	@Override
	protected void reset() {
		super.reset();
		key = -1;
	}
	
	public boolean isSatisfied() {
		return key == graph.key;
	}
	
	protected boolean setSatisfied(int level) {
		if(isSatisfied())
			return false;
		else {
			key = graph.key;
			return true;
		}
	}
}
