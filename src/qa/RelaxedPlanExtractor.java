package qa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import qa.Explanation.CausalChain;
import sabre.Agent;
import sabre.Domain;
import sabre.graph.PlanGraph;
import sabre.graph.PlanGraphActionNode;
import sabre.graph.PlanGraphLiteralNode;
import sabre.graph.PlanGraphNode;
import sabre.logic.Expression;
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
			for (PlanGraphNode actionNode : goalLiteral.parents) {
				PlanGraphActionNode action = (PlanGraphActionNode) actionNode;

				ArrayList<PlanGraphLiteralNode> newGoalLiterals = new ArrayList<>(localGoalLiterals);
				newGoalLiterals.remove(goalLiteral);

				ImmutableArray<? extends Literal> newLiterals = action.parents.get(0).clause.arguments;
				for (Literal newLiteral : newLiterals)
					newGoalLiterals.add(action.graph.getLiteral(newLiteral));

				if (!canBeExplainedByAtLeastOneConsentingCharacter(action, explanations))
					continue;

				RelaxedPlan planWithNewAction = plan.clone();
				planWithNewAction.push(action);

				ArrayList<Explanation> newExplanations = cloneExplanation(explanations, action);

				Collection<RelaxedPlan> newPlan = GetAllPossiblePlanGraphPlans(plans, planWithNewAction,
						newGoalLiterals, initialGoalLiterals, newExplanations);
				
				if (newPlan != plans)
					plans.addAll(newPlan);
			}
		}

		return plans;
	}

	private static ArrayList<Explanation> cloneExplanation(ArrayList<Explanation> explanations,
			PlanGraphActionNode action) {
		ArrayList<Explanation> newExplanations = new ArrayList<>();
		for (Explanation explanation : explanations) {
			if (explanation.containsEffect(action.event)) {
				Explanation newExplanation = explanation.clone();
				newExplanation.applyAction(action.event);
				newExplanations.add(newExplanation);
			} else
				newExplanations.add(explanation);

		}
		return newExplanations;
	}

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
