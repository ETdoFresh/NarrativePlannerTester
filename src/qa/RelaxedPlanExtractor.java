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

	static HashSet<PlanGraphLiteralNode> GetAllPreconditions(HashSet<RelaxedNode> set) {
		HashSet<PlanGraphLiteralNode> literals = new HashSet<>();
		for (RelaxedNode node : set)
			for (PlanGraphClauseNode clauseNode : node.eventNode.parents)
				for (Literal literal : clauseNode.clause.arguments)
					literals.add(node.eventNode.graph.getLiteral(literal));

		return literals;
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
			ArrayList<HashSet<RelaxedNode>> sets = new ArrayList<>();
			GetAllPossibleSteps(new ArrayList<>(goalsAtThisLevel), level, 0, explanations, new HashSet<>(), sets);
			for (HashSet<RelaxedNode> set : sets) {
				HashSet<PlanGraphLiteralNode> newGoals = GetAllPreconditions(set);
				int previousLevel = level - 1;
				RelaxedPlan planWithSet = plan.clone();
				planWithSet.pushAll(set);
				for (RelaxedNode node : set)
					GetAllPossiblePlans(newGoals, previousLevel, node.explanations, planWithSet, plans);
			}
		}
	}

	static void GetAllPossibleSteps(ArrayList<PlanGraphLiteralNode> goalsAtThisLevel, int level, int i,
			ArrayList<Explanation> explanations, HashSet<RelaxedNode> set, ArrayList<HashSet<RelaxedNode>> sets) {

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
			PlanGraphEventNode eventNode, ArrayList<Explanation> explanations, HashSet<RelaxedNode> set,
			ArrayList<HashSet<RelaxedNode>> sets) {
		PlanGraphActionNode actionNode = eventNode instanceof PlanGraphActionNode ? (PlanGraphActionNode) eventNode
				: null;
		if (actionNode == null || j == actionNode.event.agents.size()) {
			int nextI = i + 1;
			HashSet<RelaxedNode> newSet = new HashSet<>(set);
			newSet.add(new RelaxedNode(eventNode, explanations, level));
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

	static void GetAllPossibleClassicalPlans(SearchSpace space, Iterable<? extends Literal> goal,
			ArrayList<RelaxedPlan> plans) {
		HashSet<PlanGraphLiteralNode> goalLiterals = new HashSet<>(getGoalLiterals(space.graph, goal));
		GetAllPossibleClassicalPlans(goalLiterals, space.graph.size() - 1, new RelaxedPlan(), plans);
	}

	static void GetAllPossibleClassicalPlans(HashSet<PlanGraphLiteralNode> goalsAtThisLevel, int level,
			RelaxedPlan plan, ArrayList<RelaxedPlan> plans) {
		if (level == 0) {
			plans.add(plan);
		} else {
			ArrayList<HashSet<RelaxedNode>> sets = new ArrayList<>();
			GetAllPossibleClassicalSteps(new ArrayList<>(goalsAtThisLevel), level, 0, new HashSet<>(), sets);
			for (HashSet<RelaxedNode> set : sets) {
				HashSet<PlanGraphLiteralNode> newGoals = GetAllPreconditions(set);
				int previousLevel = level - 1;
				RelaxedPlan planWithSet = plan.clone();
				planWithSet.pushAll(set);
				GetAllPossibleClassicalPlans(newGoals, previousLevel, planWithSet, plans);
			}
		}
	}

	static void GetAllPossibleClassicalSteps(ArrayList<PlanGraphLiteralNode> goalsAtThisLevel, int level, int i,
			HashSet<RelaxedNode> set, ArrayList<HashSet<RelaxedNode>> sets) {

		if (i == goalsAtThisLevel.size()) {
			sets.add(set);

		} else {
			for (PlanGraphNode node : goalsAtThisLevel.get(i).parents) {
				if (node.getLevel() > level)
					continue;

				if (node instanceof PlanGraphEventNode) {
					PlanGraphEventNode eventNode = (PlanGraphEventNode) node;
					int nextI = i + 1;
					HashSet<RelaxedNode> newSet = new HashSet<>(set);
					newSet.add(new RelaxedNode(eventNode, null, level));
					GetAllPossibleClassicalSteps(goalsAtThisLevel, level, nextI, newSet, sets);
				}
			}
		}
	}
}
