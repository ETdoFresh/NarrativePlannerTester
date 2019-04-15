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
		ArrayList<RelaxedPlanLiteralNode> goalLiterals = getGoalLiterals(space.graph, goal);
		ArrayList<Explanation> explanations = getExplanations(space.domain);
		ArrayList<ArrayList<RelaxedPlanEventNode>> plans = new ArrayList<>();
		ArrayList<RelaxedPlanEventNode> plan = new ArrayList<>();
		plans.add(plan);
		plans = GetAllPossiblePlanGraphPlans(plans, plan, goalLiterals, goalLiterals, explanations);
		return GetRelaxedPlans(plans);
	}

	private static Collection<RelaxedPlan> GetRelaxedPlans(ArrayList<ArrayList<RelaxedPlanEventNode>> plans) {
		ArrayList<RelaxedPlan> relaxedPlans = new ArrayList<>();
		for (ArrayList<RelaxedPlanEventNode> plan : plans) {
			RelaxedPlan relaxedPlan = new RelaxedPlan();
			for (RelaxedPlanEventNode event : plan)
				relaxedPlan.push(event.node);
		}
		return relaxedPlans;
	}

	private static ArrayList<Explanation> getExplanations(Domain domain) {
		ArrayList<Explanation> explanations = new ArrayList<>();
		for (Agent agent : domain.agents)
			explanations.add(new Explanation(agent, AgentGoal.get(domain, agent)));

		return explanations;
	}

	private static ArrayList<RelaxedPlanLiteralNode> getGoalLiterals(PlanGraph graph,
			Iterable<? extends Literal> goal) {
		ArrayList<RelaxedPlanLiteralNode> planGraphGoal = new ArrayList<>();
		for (Literal literal : goal)
			planGraphGoal.add(new RelaxedPlanLiteralNode(graph.getLiteral(literal), graph.size() - 1));
		return planGraphGoal;
	}

	static ArrayList<ArrayList<RelaxedPlanEventNode>> GetAllPossiblePlanGraphPlans(
			ArrayList<ArrayList<RelaxedPlanEventNode>> plans, ArrayList<RelaxedPlanEventNode> plan,
			ArrayList<RelaxedPlanLiteralNode> localGoalLiterals, ArrayList<RelaxedPlanLiteralNode> initialGoalLiterals,
			ArrayList<Explanation> explanations) {

		// If GoalLiterals Size is 0, we are done! Add that plan!
		if (localGoalLiterals.size() == 0)
			return new ArrayList<ArrayList<RelaxedPlanEventNode>>(Arrays.asList(plan));

		// For each Goal Literal, follow its parents.
		for (RelaxedPlanLiteralNode goalLiteral : localGoalLiterals) {
			for (RelaxedPlanEventNode event : goalLiteral.parents()) {
				if (event.node instanceof PlanGraphActionNode) {

					if (event.level > goalLiteral.level)
						continue;

					if (!selectOnlyOneEventPerLayer(event, plan))
						continue;

					// Removing Filter as this may be excessive/unneeded
					if (plan.contains(event))
						continue;

					PlanGraphActionNode actionNode = (PlanGraphActionNode) event.node;
					if (!canBeExplainedForAllConsentingCharacters(actionNode, explanations))
						continue;

					// Removing Filter as this may be excessive/unneeded
//					if (!canExtendAtLeastOneChain(explanations, actionNode))
//						continue;

					ArrayList<RelaxedPlanLiteralNode> newGoalLiterals = new ArrayList<>(localGoalLiterals);

					// Remove all effects of chosen action from newGoalLiterals
					for (RelaxedPlanLiteralNode child : event.children())
						if (newGoalLiterals.contains(child))
							newGoalLiterals.remove(child);

					// Add preconditions as newGoalLiterals
					for (RelaxedPlanLiteralNode preconditionLiteral : event.parents())
						if (!newGoalLiterals.contains(preconditionLiteral))
							newGoalLiterals.add(preconditionLiteral);

					// Remove Initial State Literals from NewGoalLiterals
					for (int i = newGoalLiterals.size() - 1; i >= 0; i--) {
						RelaxedPlanLiteralNode newGoalLiteral = newGoalLiterals.get(i);
						if (newGoalLiteral.node.getLevel() == 0)
							newGoalLiterals.remove(newGoalLiteral);
					}

//					if (stillContainsRelaxedLevelZeroLiteral(newGoalLiterals))
//						continue;

					ArrayList<RelaxedPlanEventNode> planWithNewEvent = new ArrayList<>(plan); // clone()
					planWithNewEvent.add(0, event); // push()
					ArrayList<Explanation> newExplanations = cloneExplanation(explanations, actionNode);

					// Removing Filter as this may be excessive/unneeded
					// Just trying another pruning method...
//					for (Explanation newExplanation : newExplanations)
//						newExplanation.noveltyPruneChains();

					ArrayList<ArrayList<RelaxedPlanEventNode>> newPlan = GetAllPossiblePlanGraphPlans(plans,
							planWithNewEvent, newGoalLiterals, initialGoalLiterals, newExplanations);
					if (newPlan != plans)
						plans.addAll(newPlan);
				} else {
					// TODO: Handle axioms (but not yet; no axioms in Camelot domain now)
				}
			}
		}
		return plans;
	}

	private static boolean selectOnlyOneEventPerLayer(RelaxedPlanEventNode event,
			ArrayList<RelaxedPlanEventNode> plan) {
		int minLevel = Integer.MAX_VALUE;
		for (RelaxedPlanEventNode eventNode : plan)
			if (minLevel > eventNode.level)
				minLevel = eventNode.level;

		if (event.level >= minLevel)
			return false;
		else
			return true;
	}

	private static boolean stillContainsRelaxedLevelZeroLiteral(ArrayList<RelaxedPlanLiteralNode> newGoalLiterals) {
		for (RelaxedPlanLiteralNode literalNode : newGoalLiterals)
			if (literalNode.level == 0)
				return true;

		return false;
	}

	private static boolean canExtendAtLeastOneChain(ArrayList<Explanation> explanations,
			PlanGraphActionNode actionNode) {
		for (Explanation explanation : explanations)
			if (explanation.canExtendAtLeastOneChain(actionNode))
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
