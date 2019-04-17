package qa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import sabre.Agent;
import sabre.Domain;
import sabre.graph.PlanGraph;
import sabre.graph.PlanGraphActionNode;
import sabre.graph.PlanGraphAxiomNode;
import sabre.graph.PlanGraphClauseNode;
import sabre.graph.PlanGraphEventNode;
import sabre.graph.PlanGraphLiteralNode;
import sabre.graph.PlanGraphNode;
import sabre.logic.ConjunctiveClause;
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

		return computeLiteralsIndividuallyAndUnionCombinations(goalLiterals, explanations, maxLevel);

		// Use this if you don't want to use the union each plan algorithm above.
		// return GetAllPossiblePlanGraphPlans(new ArrayList<RelaxedPlan>(), new
		// RelaxedPlan(), goalLiterals, goalLiterals, explanations, maxLevel);
	}

	private static Collection<RelaxedPlan> computeLiteralsIndividuallyAndUnionCombinations(
			ArrayList<PlanGraphLiteralNode> goalLiterals, ArrayList<Explanation> explanations, int maxLevel) {
		// Create plans for each literal, and get combination of all relaxed union plans
		// Union removes same step from happening multiple times.
		ArrayList<Collection<RelaxedPlan>> plans = new ArrayList<>();
		for (PlanGraphLiteralNode goalLiteral : goalLiterals) {
			ArrayList<PlanGraphLiteralNode> initialGoalLiteral = new ArrayList<>();
			initialGoalLiteral.add(goalLiteral);
			plans.add(GetAllPossiblePlanGraphPlans(new ArrayList<RelaxedPlan>(), new RelaxedPlan(), initialGoalLiteral,
					initialGoalLiteral, explanations, maxLevel));
		}
		ArrayList<RelaxedPlan> unionedPlans = getCombinedPlans(0, plans, new RelaxedPlan());

		// Remove Duplicate Plans
		for (int i = unionedPlans.size() - 1; i >= 0; i--)
			for (int j = i - 1; j >= 0; j--)
				if (unionedPlans.get(i).size() == unionedPlans.get(j).size())
					if (unionedPlans.get(i).intersection(unionedPlans.get(j)) == unionedPlans.get(i).size()) {
						unionedPlans.remove(i);
						break;
					}
		return unionedPlans;
	}

	private static ArrayList<RelaxedPlan> getCombinedPlans(int i, ArrayList<Collection<RelaxedPlan>> listOfPlans,
			RelaxedPlan newPlan) {
		ArrayList<RelaxedPlan> newListOfPlans = new ArrayList<>();

		if (i == listOfPlans.size()) {
			newListOfPlans.add(newPlan);
			return newListOfPlans;
		}

		Collection<RelaxedPlan> plans = listOfPlans.get(i);
		for (RelaxedPlan plan : plans) {
			newListOfPlans.addAll(getCombinedPlans(i + 1, listOfPlans, newPlan.unionClone(plan)));
		}
		return newListOfPlans;
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
	
	static void GetAllPossiblePlans(SearchSpace space, Iterable<? extends Literal> goal, ArrayList<RelaxedPlan> plans) {
		HashSet<PlanGraphLiteralNode> goalLiterals = new HashSet<>(getGoalLiterals(space.graph, goal));
		ArrayList<Explanation> explanations = getExplanations(space.domain);
		GetAllPossiblePlans(goalLiterals, space.graph.size()-1, explanations, new RelaxedPlan(), plans);
	}

	static void GetAllPossiblePlans(HashSet<PlanGraphLiteralNode> goalsAtThisLevel, int level,
			ArrayList<Explanation> explanations, RelaxedPlan plan, ArrayList<RelaxedPlan> plans) {
		if (level == 0) {
			plans.add(plan);
			System.out.println(plan);
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
		for(PlanGraphEventNode step : set)
			for (PlanGraphClauseNode clauseNode : step.parents)
				for (Literal literal : clauseNode.clause.arguments)
					literals.add(step.graph.getLiteral(literal));
		
		return literals;
	}
	
	static void GetAllPossibleSteps(ArrayList<PlanGraphLiteralNode> goalsAtThisLevel, int level, int i,
			ArrayList<Explanation> explanations, HashSet<PlanGraphEventNode> set, ArrayList<HashSet<PlanGraphEventNode>> sets) {
		
		if (i == goalsAtThisLevel.size()) {
			sets.add(set);
		} else {
			for (PlanGraphNode node : goalsAtThisLevel.get(i).parents) {
				if (node.getLevel() > level)
					continue;
				
				if (node instanceof PlanGraphEventNode) {
					int nextI = i + 1;
					PlanGraphEventNode eventNode = (PlanGraphEventNode) node;
					HashSet<PlanGraphEventNode> newSet = new HashSet<>(set);
					newSet.add(eventNode);
					GetAllPossibleSteps(goalsAtThisLevel, level, nextI, explanations, newSet, sets);
				}
			}
		}
	}

	static Collection<RelaxedPlan> GetAllPossiblePlanGraphPlans(ArrayList<RelaxedPlan> plans, RelaxedPlan plan,
			ArrayList<PlanGraphLiteralNode> localGoalLiterals, ArrayList<PlanGraphLiteralNode> initialGoalLiterals,
			ArrayList<Explanation> explanations, int maxLevel) {

		// Remove Initial State Literals from localGoalLiterals
		// Only doing this if goal is true in initial state
		localGoalLiterals = new ArrayList<>(localGoalLiterals); // copy()
		for (int i = localGoalLiterals.size() - 1; i >= 0; i--) {
			PlanGraphLiteralNode localGoalLiteral = localGoalLiterals.get(i);
			if (localGoalLiteral.getLevel() == 0)
				localGoalLiterals.remove(localGoalLiteral);
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
//					if (!canExtendAtLeastOneChain(explanations, actionNode))
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

					// Remove Initial State Literals from newGoalLiterals
//					for (int i = newGoalLiterals.size() - 1; i >= 0; i--) {
//						PlanGraphLiteralNode newGoalLiteral = newGoalLiterals.get(i);
//						if (newGoalLiteral.getLevel() == 0)
//							newGoalLiterals.remove(newGoalLiteral);
//					}

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
