package qa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import sabre.Agent;
import sabre.Domain;
import sabre.graph.PlanGraph;
import sabre.graph.PlanGraphActionNode;
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
		return RelaxedPlanExtractor.GetAllPossiblePlanGraphPlans(new ArrayList<RelaxedPlan>(), new RelaxedPlan(),
				goalLiterals, goalLiterals, explanations);
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
			ArrayList<Explanation> explanations) {
		
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
				ArrayList<PlanGraphLiteralNode> newGoalLiterals = new ArrayList<>(localGoalLiterals);
				newGoalLiterals.remove(goalLiteral);
				if(node instanceof PlanGraphActionNode) {
					PlanGraphActionNode actionNode = (PlanGraphActionNode) node;
					ImmutableArray<? extends Literal> newLiterals = actionNode.parents.get(0).clause.arguments;
					for (Literal newLiteral : newLiterals)
						newGoalLiterals.add(actionNode.graph.getLiteral(newLiteral));
					if (!canBeExplainedForAllConsentingCharacters(actionNode, explanations))
						continue;
					RelaxedPlan planWithNewEvent = plan.clone();
					planWithNewEvent.push(actionNode);
					ArrayList<Explanation> newExplanations = cloneExplanation(explanations, actionNode);
					Collection<RelaxedPlan> newPlan = GetAllPossiblePlanGraphPlans(plans, planWithNewEvent,
							newGoalLiterals, initialGoalLiterals, newExplanations);
					if (newPlan != plans)
						plans.addAll(newPlan);
				} else {
					// TODO: Handle axioms (but not yet; no axioms in Camelot domain now)
				}
			}
		}
		return plans;
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
	
	private static boolean canBeExplainedForAllConsentingCharacters(PlanGraphActionNode node, 
			ArrayList<Explanation> explanations) {
		for (Term agent : node.event.agents) {
			boolean explained = false;
			for (Explanation explanation : explanations) {
				if (explanation.agent.equals(agent))
					if (explanation.containsEffect(node.event))
						explained = true;
			}
			if(!explained)
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
