package qa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import sabre.Agent;
import sabre.Domain;
import sabre.graph.PlanGraph;
import sabre.graph.PlanGraphActionNode;
import sabre.graph.PlanGraphAxiomNode;
import sabre.graph.PlanGraphEventNode;
import sabre.graph.PlanGraphLiteralNode;
import sabre.graph.PlanGraphNode;
import sabre.logic.Literal;
import sabre.logic.Term;
import sabre.space.SearchSpace;
import sabre.util.ImmutableArray;

public class RelaxedPlanExtractor {

	// Returns a list of all possible PlanGraph Plans
	static Collection<RelaxedPlan> GetAllPossiblePlanGraphPlans(SearchSpace space, Iterable<? extends Literal> goal) {
		ArrayList<PlanGraphLiteralNode> goalLiterals = getGoalLiterals(space.graph, goal);
		ArrayList<Explanation> explanations = getExplanations(space.domain);
		int maxLevel = space.graph.size();
		return RelaxedPlanExtractor.GetAllPossiblePlanGraphPlans(new ArrayList<RelaxedPlan>(), new RelaxedPlan(),
				goalLiterals, goalLiterals, explanations, maxLevel);
	}

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

	static Collection<RelaxedPlan> GetAllPossiblePlanGraphPlans(ArrayList<RelaxedPlan> plans, RelaxedPlan plan,
			ArrayList<PlanGraphLiteralNode> localGoalLiterals, ArrayList<PlanGraphLiteralNode> initialGoalLiterals,
			ArrayList<Explanation> explanations, int maxLevel) {

		// Remove Initial State Literals from GoalLiterals
		for (int i = localGoalLiterals.size() - 1; i >= 0; i--) {
			PlanGraphLiteralNode goalLiteral = localGoalLiterals.get(i);
			if (goalLiteral.getLevel() == 0)
				localGoalLiterals.remove(goalLiteral);
		}

		// If GoalLiterals Size is 0, we are done! Add that plan!
		if (localGoalLiterals.size() == 0) {
			return new ArrayList<RelaxedPlan>(Arrays.asList(plan));
		}

		// For each Goal Literal, follow its parents.
		for (PlanGraphLiteralNode goalLiteral : localGoalLiterals) {
			for (PlanGraphNode node : goalLiteral.parents) {
				if (node instanceof PlanGraphActionNode) {
					
					// Only consider plans the size of plangraph
					if (node.getLevel() > maxLevel)
						continue;
					
					// Removing Filter as this may be excessive/unneeded
//					if (actionAlreadyExistsIn(plan, node))
//						continue;

					PlanGraphActionNode actionNode = (PlanGraphActionNode) node;
					if (!canBeExplainedForAllConsentingCharacters(actionNode, explanations))
						continue;

					// Removing Filter as this may be excessive/unneeded
//					if (!canExtendAtLeastOneCluster(explanations, actionNode))
//						continue;

					ArrayList<PlanGraphLiteralNode> newGoalLiterals = new ArrayList<>(localGoalLiterals);
					
					// Remove all effects of chosen action from newGoalLiterals
					for (PlanGraphNode child : actionNode.children)
						if (child instanceof PlanGraphLiteralNode)
							if (newGoalLiterals.contains(child))
								newGoalLiterals.remove(child);

					// Add preconditions as newGoalLiterals
					ImmutableArray<? extends Literal> newLiterals = actionNode.parents.get(0).clause.arguments;
					for (Literal newLiteral : newLiterals)
						newGoalLiterals.add(actionNode.graph.getLiteral(newLiteral));

					RelaxedPlan planWithNewEvent = plan.clone();
					planWithNewEvent.push(actionNode);
					ArrayList<Explanation> newExplanations = cloneExplanation(explanations, actionNode);

					// Removing Filter as this may be excessive/unneeded
					// Just trying another pruning method...
//					for (Explanation newExplanation : newExplanations)
//						newExplanation.noveltyPruneChains();

					Collection<RelaxedPlan> newPlan = GetAllPossiblePlanGraphPlans(plans, planWithNewEvent,
							newGoalLiterals, initialGoalLiterals, newExplanations, maxLevel - 1);
					if (newPlan != plans)
						plans.addAll(newPlan);
				} else {
					// TODO: Handle axioms (but not yet; no axioms in Camelot domain now)
				}
			}
		}
		return plans;
	}

	private static boolean actionAlreadyExistsIn(RelaxedPlan plan, PlanGraphNode node) {
		return plan.contains(((PlanGraphActionNode) node).event);
	}

	private static boolean canExtendAtLeastOneCluster(ArrayList<Explanation> explanations,
			PlanGraphActionNode actionNode) {
		for (Explanation explanation : explanations)
			if (explanation.canExtendAtLeastOneCluster(actionNode))
				return true;

		return false;
	}

	private static void DebugThis(ArrayList<RelaxedPlan> plans, RelaxedPlan plan,
			ArrayList<PlanGraphLiteralNode> localGoalLiterals, ArrayList<PlanGraphLiteralNode> initialGoalLiterals,
			ArrayList<Explanation> explanations) {
		try {

			// This passed our filters!!!

			BufferedWriter writer = new BufferedWriter(new FileWriter("DebugThis.txt", true));
			// writer.append("plans: " + plans + "\n");
			writer.append("plan: " + plan);
			writer.append("localGoalLiterals: " + localGoalLiterals + "\n");
			// writer.append("initialGoalLiterals: " + initialGoalLiterals + "\n");
			writer.append("explanations: " + explanations + "\n");
			writer.append("-----------------------------------------------------------" + "\n");
			writer.append("" + "\n");
			writer.close();
		} catch (Exception ex) {
		}
	}

	private static ArrayList<Explanation> cloneExplanation(ArrayList<Explanation> explanations,
			PlanGraphEventNode action) {
		ArrayList<Explanation> newExplanations = new ArrayList<>();
		for (Explanation explanation : explanations) {
			if (explanation.containsEffect(action.event)) {
				Explanation newExplanation = explanation.clone();
				newExplanation.applyEvent(action.event);
				newExplanations.add(newExplanation);
			} else
				newExplanations.add(explanation);

		}
		return newExplanations;
	}

	private static boolean canBeExplainedForAllConsentingCharacters(PlanGraphEventNode eventNode,
			ArrayList<Explanation> explanations) {
		if (eventNode instanceof PlanGraphAxiomNode)
			return true;
		PlanGraphActionNode actionNode = (PlanGraphActionNode) eventNode;
		for (Term agent : actionNode.event.agents) {
			boolean explained = false;
			for (Explanation explanation : explanations) {
				if (explanation.agent.equals(agent))
					if (explanation.containsEffect(actionNode.event)) {
						explained = true;
						break;
					}
			}
			if (!explained)
				return false;
		}
		return true;
	}

	// this is not the right question
	private static boolean canBeExplainedByAtLeastOneConsentingCharacter(PlanGraphActionNode actionNode,
			ArrayList<Explanation> explanations) {
		for (Term agent : actionNode.event.agents)
			for (Explanation explanation : explanations)
				if (explanation.agent.equals(agent))
					if (explanation.containsEffect(actionNode.event))
						return true;

		return false;
	}
}
