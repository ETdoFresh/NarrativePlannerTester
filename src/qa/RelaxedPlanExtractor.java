package qa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import sabre.Agent;
import sabre.Domain;
import sabre.graph.PlanGraph;
import sabre.graph.PlanGraphActionNode;
import sabre.graph.PlanGraphClauseNode;
import sabre.graph.PlanGraphEventNode;
import sabre.graph.PlanGraphLiteralNode;
import sabre.graph.PlanGraphNode;
import sabre.logic.Assignment;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Expression;
import sabre.logic.Literal;
import sabre.logic.NegatedLiteral;
import sabre.logic.Term;
import sabre.space.SearchSpace;

public class RelaxedPlanExtractor {

	static ArrayList<RelaxedPlan> GetAllPossiblePlans(SearchSpace space, Expression goals) {
		ArrayList<RelaxedPlan> plans = new ArrayList<>();
		for (ConjunctiveClause goal : goals.toDNF().arguments) {
			HashSet<PlanGraphLiteralNode> goalLiterals = new HashSet<>(getGoalLiterals(space.graph, goal.arguments));
			ArrayList<Explanation> explanations = getExplanations(space.domain);
			GetAllPossiblePlans(goalLiterals, space.graph.size() - 1, explanations, new RelaxedPlan(), plans);
		}
		return plans;
	}

	static ArrayList<RelaxedPlan> GetAllPossibleClassicalPlans(SearchSpace space, Expression goals) {
		ArrayList<RelaxedPlan> plans = new ArrayList<>();
		for (ConjunctiveClause goal : goals.toDNF().arguments) {
			HashSet<PlanGraphLiteralNode> goalLiterals = new HashSet<>(getGoalLiterals(space.graph, goal.arguments));
			GetAllPossibleClassicalPlans(goalLiterals, space.graph.size() - 1, new RelaxedPlan(), plans);
		}
		return plans;
	}

	private static ArrayList<PlanGraphLiteralNode> getGoalLiterals(PlanGraph graph, Iterable<? extends Literal> goal) {
		ArrayList<PlanGraphLiteralNode> planGraphGoal = new ArrayList<>();
		for (Literal literal : goal)
			if (literal instanceof NegatedLiteral) {
				NegatedLiteral negatedLiteral = (NegatedLiteral) literal;
				if (negatedLiteral.argument instanceof Assignment) {
					Assignment assignment = (Assignment) negatedLiteral.argument;
					for (PlanGraphLiteralNode otherLiteral : graph.literals)
						if (otherLiteral.literal instanceof Assignment) {
							Assignment otherAssignment = (Assignment) otherLiteral.literal;
							if (assignment.property.equals(otherAssignment.property))
								if (assignment.arguments.equals(otherAssignment.arguments))
									if (!assignment.value.equals(otherAssignment.value))
										planGraphGoal.add(otherLiteral);
						}
				}
			} else
				planGraphGoal.add(graph.getLiteral(literal));
		return planGraphGoal;
	}

	private static ArrayList<Explanation> getExplanations(Domain domain) {
		ArrayList<Explanation> explanations = new ArrayList<>();
		for (Agent agent : domain.agents)
			explanations.add(new Explanation(agent, AgentGoal.get(domain, agent)));

		return explanations;
	}

	private static HashSet<PlanGraphLiteralNode> GetAllPreconditions(HashSet<RelaxedNode> set) {
		HashSet<PlanGraphLiteralNode> literals = new HashSet<>();
		for (RelaxedNode node : set)
			for (PlanGraphClauseNode clauseNode : node.eventNode.parents)
				for (Literal literal : clauseNode.clause.arguments)
					literals.add(node.eventNode.graph.getLiteral(literal));

		return literals;
	}

	private static ArrayList<Explanation> replace(Explanation oldExplanation, Explanation newExplanation,
			ArrayList<Explanation> explanations) {
		ArrayList<Explanation> newExplanations = new ArrayList<>(explanations);
		newExplanations.remove(oldExplanation);
		newExplanations.add(newExplanation);
		return newExplanations;
	}

	private static void GetAllPossiblePlans(HashSet<PlanGraphLiteralNode> goalsAtThisLevel, int level,
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

	private static void GetAllPossibleSteps(ArrayList<PlanGraphLiteralNode> goalsAtThisLevel, int level, int i,
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

	private static void GetAllWaysToConsent(ArrayList<PlanGraphLiteralNode> goalsAtThisLevel, int level, int i, int j,
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

	private static void GetAllPossibleClassicalPlans(HashSet<PlanGraphLiteralNode> goalsAtThisLevel, int level,
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

	private static void GetAllPossibleClassicalSteps(ArrayList<PlanGraphLiteralNode> goalsAtThisLevel, int level, int i,
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

	static ArrayList<RelaxedPlan> GetAllPossiblePGEPlans(SearchSpace space, Expression goals,
			HashMap<Agent, ArrayList<RelaxedPlan>> agentsPlans) {
		ArrayList<RelaxedPlan> plans = new ArrayList<>();
		for (ConjunctiveClause goal : goals.toDNF().arguments) {
			HashSet<PlanGraphLiteralNode> goalLiterals = new HashSet<>(getGoalLiterals(space.graph, goal.arguments));
			GetAllPossiblePGEPlans(goalLiterals, space.graph.size() - 1, agentsPlans, new RelaxedPlan(), plans);
		}
		return plans;
	}

	private static void GetAllPossiblePGEPlans(HashSet<PlanGraphLiteralNode> goalsAtThisLevel, int level,
			HashMap<Agent, ArrayList<RelaxedPlan>> agentsPlans, RelaxedPlan plan, ArrayList<RelaxedPlan> plans) {
		if (level == 0) {
			plans.add(plan);
		} else {
			ArrayList<HashSet<RelaxedNode>> sets = new ArrayList<>();
			GetAllPossiblePGESteps(new ArrayList<>(goalsAtThisLevel), level, agentsPlans, 0, new HashSet<>(), sets);
			for (HashSet<RelaxedNode> set : sets) {
				HashSet<PlanGraphLiteralNode> newGoals = GetAllPreconditions(set);
				int previousLevel = level - 1;
				RelaxedPlan planWithSet = plan.clone();
				planWithSet.pushAll(set);
				GetAllPossiblePGEPlans(newGoals, previousLevel, agentsPlans, planWithSet, plans);
			}
		}
	}

	private static void GetAllPossiblePGESteps(ArrayList<PlanGraphLiteralNode> goalsAtThisLevel, int level,
			HashMap<Agent, ArrayList<RelaxedPlan>> agentsPlans, int i, HashSet<RelaxedNode> set,
			ArrayList<HashSet<RelaxedNode>> sets) {

		if (i == goalsAtThisLevel.size()) {
			sets.add(set);

		} else {
			for (PlanGraphNode node : goalsAtThisLevel.get(i).parents) {
				if (node.getLevel() > level)
					continue;

				if (!explainedByAllConsentingCharacters(node, level, agentsPlans))
					continue;

				if (node instanceof PlanGraphEventNode) {
					PlanGraphEventNode eventNode = (PlanGraphEventNode) node;
					int nextI = i + 1;
					HashSet<RelaxedNode> newSet = new HashSet<>(set);
					newSet.add(new RelaxedNode(eventNode, null, level));
					GetAllPossiblePGESteps(goalsAtThisLevel, level, agentsPlans, nextI, newSet, sets);
				}
			}
		}
	}

	private static boolean explainedByAllConsentingCharacters(PlanGraphNode node, int level,
			HashMap<Agent, ArrayList<RelaxedPlan>> agentsPlans) {
		if (!(node instanceof PlanGraphActionNode))
			return true;

		PlanGraphActionNode actionNode = (PlanGraphActionNode) node;
		for (Term term : actionNode.event.agents) {
			Agent agent = (Agent) term;
			boolean foundConsentingAction = false;
			for (RelaxedPlan agentPlan : agentsPlans.get(agent))
				if (!foundConsentingAction)
					for (RelaxedNode planNode : agentPlan)
						if (planNode.eventNode.equals(actionNode))
							if (planNode.level == level) {
								foundConsentingAction = true;
								break;
							}
			if (!foundConsentingAction)
				return false;
		}
		return true;
	}
}
