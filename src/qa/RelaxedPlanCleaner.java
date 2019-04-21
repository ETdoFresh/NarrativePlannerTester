package qa;

import java.util.ArrayList;
import java.util.HashSet;

import sabre.graph.PlanGraphClauseNode;
import sabre.graph.PlanGraphLiteralNode;
import sabre.graph.PlanGraphNode;
import sabre.logic.ConjunctiveClause;
import sabre.space.SearchSpace;

public class RelaxedPlanCleaner {
	public static void RemoveDuplicates(ArrayList<RelaxedPlan> plans) {
		for (int i = plans.size() - 1; i >= 0; i--)
			for (int j = i - 1; j >= 0; j--)
				if (plans.get(i).equals(plans.get(j))) {
					plans.remove(i);
					break;
				}
	}

	public static void StopStoryAfterOneAuthorGoalComplete(SearchSpace space, ArrayList<RelaxedPlan> plans) {
		HashSet<HashSet<PlanGraphLiteralNode>> authorGoals = new HashSet<>();
		for (ConjunctiveClause goal : space.goal.toDNF().arguments) {
			HashSet<PlanGraphLiteralNode> goalLiterals = new HashSet<>(
					RelaxedPlanExtractor.getGoalLiterals(space.graph, goal.arguments));
			authorGoals.add(goalLiterals);
		}

		for (RelaxedPlan plan : plans) {
			HashSet<PlanGraphLiteralNode> achievedEffects = RelaxedPlanExtractor.getInitialLiterals(space.graph);
			for (int i = 0; i < plan.size(); i++) {
				if (achievesAuthorGoal(achievedEffects, authorGoals)) {
					for (int j = plan.size() - 1; j >= i; j--)
						plan.remove(j);
					break;
				}
				for (PlanGraphNode node : plan.get(i).eventNode.children)
					if (node instanceof PlanGraphClauseNode)
						for (PlanGraphLiteralNode effect : ((PlanGraphClauseNode) node).parents)
							achievedEffects.add(effect);
					else if (node instanceof PlanGraphLiteralNode)
						achievedEffects.add((PlanGraphLiteralNode) node);
			}
		}
	}

	private static boolean achievesAuthorGoal(HashSet<PlanGraphLiteralNode> achievedEffects,
			HashSet<HashSet<PlanGraphLiteralNode>> authorGoals) {
		for (HashSet<PlanGraphLiteralNode> authorGoal : authorGoals)
			if (achievedEffects.containsAll(authorGoal))
				return true;

		return false;
	}
}
