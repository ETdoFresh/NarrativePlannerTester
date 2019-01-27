package sabre.graph;

import sabre.Settings;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Literal;
import sabre.util.ImmutableSet;
import sabre.util.MutableSet;

public class PlanGraphClauseNode extends PlanGraphNode {
	
	private static final long serialVersionUID = Settings.VERSION_UID;

	public final ConjunctiveClause clause;
	public final ImmutableSet<PlanGraphLiteralNode> parents = new MutableSet<>(new PlanGraphLiteralNode[0]);
	public final ImmutableSet<PlanGraphSatisfiableNode> children = new MutableSet<>(new PlanGraphSatisfiableNode[0]);
	private int key = -1;
	private int count = 0;
	
	PlanGraphClauseNode(PlanGraph graph, ConjunctiveClause clause) {
		super(graph);
		this.clause = clause;
		for(Literal literal : clause.arguments) {
			PlanGraphLiteralNode parent = graph.addLiteral(literal);
			((MutableSet<PlanGraphLiteralNode>) parents).add(parent);
			((MutableSet<PlanGraphClauseNode>) parent.children).add(this);
		}
	}
	
	@Override
	public String toString() {
		return "[" + clause + "; level=" + getLevel() + "; count=" + count + "]";
	}
	
	@Override
	protected void reset() {
		super.reset();
		key = -1;
		count = 0;
	}
	
	final void increment(int level) {
		if(key != graph.key) {
			key = graph.key;
			count = 0;
		}
		count++;
		if(count == parents.size())
			setLevel(level);
	}
	
	@Override
	public boolean setLevel(int level) {
		if(super.setLevel(level)) {
			for(int i=0; i<children.size(); i++)
				children.get(i).setSatisfied(level);
			return true;
		}
		else
			return false;
	}
}
