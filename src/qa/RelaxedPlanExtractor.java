package qa;

import java.util.ArrayList;
import java.util.HashSet;

import sabre.Agent;
import sabre.Domain;
import sabre.graph.PlanGraph;
import sabre.graph.PlanGraphActionNode;
import sabre.graph.PlanGraphClauseNode;
import sabre.graph.PlanGraphEventNode;
import sabre.graph.PlanGraphLiteralNode;
import sabre.graph.PlanGraphNode;
import sabre.logic.Literal;
import sabre.space.SearchSpace;

public class RelaxedPlanExtractor {

	private static ArrayList<Explanation> getExplanations(Domain domain) {
		ArrayList<Explanation> explanations = new ArrayList<>();
		for (Agent agent : domain.agents)
			explanations.add(new Explanation(agent, AgentGoal.get(domain, agent)));

		return explanations;
	}

	private static ArrayList<PlanGraphLiteralNode> getGoalLiterals(PlanGraph graph, Iterable<? extends Literal> goal) {
		ArrayList<PlanGraphLiteralNode> planGraphGoal = new ArrayList<>();
		for (Literal literal : goal)
			planGraphGoal.add(graph.getLiteral(literal));
		return planGraphGoal;
	}

	static void GetAllPossiblePlans(SearchSpace space, Iterable<? extends Literal> goal, ArrayList<RelaxedPlan> plans) {
		HashSet<PlanGraphLiteralNode> goalLiterals = new HashSet<>(getGoalLiterals(space.graph, goal));
		ArrayList<Explanation> explanations = getExplanations(space.domain);
		GetAllPossiblePlans(goalLiterals, space.graph.size() - 1, explanations, new RelaxedPlan(), plans);
	}

	static void GetAllPossiblePlans(HashSet<PlanGraphLiteralNode> goalsAtThisLevel, int level,
			ArrayList<Explanation> explanations, RelaxedPlan plan, ArrayList<RelaxedPlan> plans) {
		if (level == 0) {
			plans.add(plan);
		} else {
			ArrayList<HashSet<PlanGraphEventNode>> sets = new ArrayList<>();
			GetAllPossibleSteps(new ArrayList<>(goalsAtThisLevel), level, 0, explanations, new HashSet<>(), sets);
			for (HashSet<PlanGraphEventNode> set : sets) {
				HashSet<PlanGraphLiteralNode> newGoals = GetAllPreconditions(set);
				int previousLevel = level - 1;
				RelaxedPlan planWithSet = plan.clone();
				planWithSet.pushAll(set);
				GetAllPossiblePlans(newGoals, previousLevel, explanations, planWithSet, plans);
			}
		}
	}

	static HashSet<PlanGraphLiteralNode> GetAllPreconditions(HashSet<PlanGraphEventNode> set) {
		HashSet<PlanGraphLiteralNode> literals = new HashSet<>();
		for (PlanGraphEventNode step : set)
			for (PlanGraphClauseNode clauseNode : step.parents)
				for (Literal literal : clauseNode.clause.arguments)
					literals.add(step.graph.getLiteral(literal));

		return literals;
	}

	static void GetAllPossibleSteps(ArrayList<PlanGraphLiteralNode> goalsAtThisLevel, int level, int i,
			ArrayList<Explanation> explanations, HashSet<PlanGraphEventNode> set,
			ArrayList<HashSet<PlanGraphEventNode>> sets) {

		if (i == goalsAtThisLevel.size()) {
			sets.add(set);

		} else {
			for (PlanGraphNode node : goalsAtThisLevel.get(i).parents) {
				if (node.getLevel() > level)
					continue;

				if (node instanceof PlanGraphEventNode) {
					PlanGraphEventNode eventNode = (PlanGraphEventNode) node;
					GetAllWaysToConsent(goalsAtThisLevel, level, i, 0, eventNode, explanations, set, sets);
				}
			}
		}
	}

	static void GetAllWaysToConsent(ArrayList<PlanGraphLiteralNode> goalsAtThisLevel, int level, int i, int j,
			PlanGraphEventNode eventNode, ArrayList<Explanation> explanations, HashSet<PlanGraphEventNode> set,
			ArrayList<HashSet<PlanGraphEventNode>> sets) {
		PlanGraphActionNode actionNode = eventNode instanceof PlanGraphActionNode ? (PlanGraphActionNode) eventNode
				: null;
		if (actionNode == null || j == actionNode.event.agents.size()) {
			int nextI = i + 1;
			HashSet<PlanGraphEventNode> newSet = new HashSet<>(set);
			newSet.add(eventNode);
			GetAllPossibleSteps(goalsAtThisLevel, level, nextI, explanations, newSet, sets);
		} else {
			for (Explanation oldExplanation : explanations) {
				if (oldExplanation.agent.equals(actionNode.event.agents.get(j))) {
					Explanation newExplanation = oldExplanation.add(actionNode);
					if (newExplanation != null)
						GetAllWaysToConsent(goalsAtThisLevel, level, i, j + 1, eventNode,
								replace(oldExplanation, newExplanation, explanations), set, sets);
				}
			}
		}
	}

	private static ArrayList<Explanation> replace(Explanation oldExplanation, Explanation newExplanation,
			ArrayList<Explanation> explanations) {
		ArrayList<Explanation> newExplanations = new ArrayList<>(explanations);
		newExplanations.remove(oldExplanation);
		newExplanations.add(newExplanation);
		return newExplanations;
	}
}
