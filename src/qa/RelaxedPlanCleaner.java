package qa;

import java.util.ArrayList;
import java.util.HashSet;

import sabre.graph.PlanGraphClauseNode;
import sabre.graph.PlanGraphLiteralNode;
import sabre.graph.PlanGraphNode;
import sabre.logic.ConjunctiveClause;
import sabre.space.SearchSpace;

public class RelaxedPlanCleaner {
	public static void removeDuplicatePlans(ArrayList<RelaxedPlan> plans) {
		for (int i = plans.size() - 1; i >= 0; i--)
			for (int j = i - 1; j >= 0; j--)
				if (plans.get(i).equals(plans.get(j))) {
					plans.remove(i);
					break;
				}
	}

	public static void removeDuplicateSteps(ArrayList<RelaxedPlan> plans) {
		for (RelaxedPlan plan : plans) {
			for (int i = 0; i < plan.size(); i++) {
				RelaxedNode step = plan.get(i);
				for (int j = plan.size() - 1; j > i; j--) {
					RelaxedNode otherStep = plan.get(j);
					if (step.eventNode.equals(otherStep.eventNode))
						plan.remove(j);
				}
			}
		}
	}

	public static void stopStoryAfterOneAuthorGoalComplete(SearchSpace space, ArrayList<RelaxedPlan> plans) {
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
	
	public static void stopStoryAfterOneAuthorGoalComplete(SearchSpace space, RelaxedPlan plan) {
		ArrayList<RelaxedPlan> plans = new ArrayList<>();
		plans.add(plan);
		stopStoryAfterOneAuthorGoalComplete(space, plans);
	}

	private static boolean achievesAuthorGoal(HashSet<PlanGraphLiteralNode> achievedEffects,
			HashSet<HashSet<PlanGraphLiteralNode>> authorGoals) {
		for (HashSet<PlanGraphLiteralNode> authorGoal : authorGoals)
			if (achievedEffects.containsAll(authorGoal))
				return true;

		return false;
	}

	/** Remove duplicate RelaxedPlans according to current distance metric */
	public static ArrayList<RelaxedPlan> deDupePlans(ArrayList<RelaxedPlan> relaxedPlans, Distance distance) {
		int previous = relaxedPlans.size();
		ArrayList<RelaxedPlan> uniquePlans = new ArrayList<>(relaxedPlans);
		for (int i = uniquePlans.size() - 1; i >= 0; i--) {
			RelaxedPlan plan = uniquePlans.get(i);
			if (!isPlanSmallest(plan, uniquePlans, distance) || !isPlanSizeUnique(plan, uniquePlans, distance))
				uniquePlans.remove(i);
		}
		
//		if (relaxedPlans.size() < 10) {
//			System.out.println("PROBLEM: Only " + relaxedPlans.size() + " unique plans using " + distance.distanceMetric
//					+ " distance");
//			System.exit(1);
//		}
		System.out.println("Deduped with " + distance.distanceMetric + " distance: " + relaxedPlans.size()
				+ " unique plans out of " + previous + ".");
		
		return uniquePlans;
	}

	private static boolean isPlanSmallest(RelaxedPlan plan, ArrayList<RelaxedPlan> plans, Distance distance) {
		for (RelaxedPlan other : plans)
			if (plan != other)
				if (distance.getDistance(plan, other, plans) == 0)
					if (other.size() < plan.size())
						return false;
		return true;
	}

	private static boolean isPlanSizeUnique(RelaxedPlan plan, ArrayList<RelaxedPlan> plans, Distance distance) {
		for (RelaxedPlan other : plans)
			if (plan != other)
				if (distance.getDistance(plan, other, plans) == 0)
					if (other.size() == plan.size())
						return false;
		return true;
	}
}
