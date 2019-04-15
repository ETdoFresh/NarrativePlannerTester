package qa;

import java.util.ArrayList;

import sabre.graph.PlanGraphEventNode;
import sabre.graph.PlanGraphLiteralNode;
import sabre.graph.PlanGraphNode;

public class RelaxedPlanLiteralNode extends RelaxedPlanNode<PlanGraphLiteralNode> {

	RelaxedPlanLiteralNode(PlanGraphLiteralNode node, int level) {
		super(node, level);
	}

	public RelaxedPlanLiteralNode(PlanGraphLiteralNode node) {
		super(node);
	}

	public Iterable<RelaxedPlanEventNode> parents() {
		ArrayList<RelaxedPlanEventNode> parents = new ArrayList<>();
		int previousLevel = Math.max(0, level - 1); // Event can never happen before level 1
		for (PlanGraphNode eventNode : node.parents)
			if (eventNode instanceof PlanGraphEventNode)
				parents.add(new RelaxedPlanEventNode((PlanGraphEventNode) eventNode, previousLevel));
		return parents;
	}
	
	public Iterable<RelaxedPlanEventNode> children() {
		ArrayList<RelaxedPlanEventNode> children = new ArrayList<>();
		for (PlanGraphNode eventNode : node.children)
			if (eventNode instanceof PlanGraphEventNode)
				children.add(new RelaxedPlanEventNode((PlanGraphEventNode) eventNode, level));
		return children;
	}
}