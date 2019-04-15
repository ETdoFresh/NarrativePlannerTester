package qa;

import java.util.ArrayList;

import sabre.graph.PlanGraphClauseNode;
import sabre.graph.PlanGraphEventNode;
import sabre.graph.PlanGraphLiteralNode;
import sabre.graph.PlanGraphNode;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Literal;

public class RelaxedPlanEventNode extends RelaxedPlanNode<PlanGraphEventNode> {

	RelaxedPlanEventNode(PlanGraphEventNode node, int level) {
		super(node, level);
	}

	public Iterable<RelaxedPlanLiteralNode> parents() {
		ArrayList<RelaxedPlanLiteralNode> parents = new ArrayList<>();
		for (PlanGraphNode literalNode : node.parents)
			if (literalNode instanceof PlanGraphLiteralNode)
				parents.add(new RelaxedPlanLiteralNode((PlanGraphLiteralNode) literalNode, level));
			else if (literalNode instanceof PlanGraphClauseNode)
				for (Literal literal : ((PlanGraphClauseNode) literalNode).clause.arguments)
					parents.add(new RelaxedPlanLiteralNode(literalNode.graph.getLiteral(literal), level));
		return parents;
	}

	public Iterable<RelaxedPlanLiteralNode> children() {
		ArrayList<RelaxedPlanLiteralNode> children = new ArrayList<>();
		for (PlanGraphNode literalNode : node.children)
			if (literalNode instanceof PlanGraphLiteralNode)
				children.add(new RelaxedPlanLiteralNode((PlanGraphLiteralNode) literalNode, level + 1));
		return children;
	}
}