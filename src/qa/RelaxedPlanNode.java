package qa;

import sabre.graph.PlanGraphNode;

public class RelaxedPlanNode<Node extends PlanGraphNode> {
	public Node node;
	public int level;

	protected RelaxedPlanNode(Node node, int level) {
			this.node = node;
			this.level = level;
		}

	protected RelaxedPlanNode(Node node) {
			this(node, node.getLevel());
		}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RelaxedPlanNode)
			return node.equals(((RelaxedPlanNode) obj).node);
		else
			return false;
	}
	
	@Override
	public String toString() {
		return "(" + level + " " + node + ")\n";
	}
}