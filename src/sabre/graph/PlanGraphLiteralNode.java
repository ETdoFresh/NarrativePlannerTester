package sabre.graph;

import sabre.Settings;
import sabre.logic.Literal;
import sabre.util.ImmutableSet;
import sabre.util.MutableSet;

public class PlanGraphLiteralNode extends PlanGraphNode {
	
	private static final long serialVersionUID = Settings.VERSION_UID;

	public final Literal literal;
	public final ImmutableSet<PlanGraphNode> parents = new MutableSet<>(new PlanGraphNode[0]);
	public final ImmutableSet<PlanGraphClauseNode> children = new MutableSet<>(new PlanGraphClauseNode[0]);
	
	PlanGraphLiteralNode(PlanGraph graph, Literal literal) {
		super(graph);
		this.literal = literal;
		graph.literalMap.put(literal, this);
	}
	
	@Override
	public String toString() {
		return "[" + literal.toString() + "; level=" + getLevel() + "]";
	}
	
	@Override
	public boolean setLevel(int level) {
		if(super.setLevel(level)) {
			graph.literals.add(this);
			for(int i=0; i<children.size(); i++)
				children.get(i).increment(level);
			return true;
		}
		else
			return false;
	}
}
