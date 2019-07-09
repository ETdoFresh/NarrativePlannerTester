package qa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import sabre.Agent;
import sabre.graph.PlanGraph;
import sabre.graph.PlanGraphClauseNode;
import sabre.graph.PlanGraphEventNode;
import sabre.graph.PlanGraphLiteralNode;
import sabre.graph.PlanGraphNode;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Expression;
import sabre.logic.Literal;
import sabre.space.SearchSpace;

public class PlanGraphExplanations {
	public static ArrayList<RelaxedPlan> getExplainedPlans(SearchSpace space, boolean onlyExploreAuthorGoals) {
		HashMap<Agent, HashSet<RelaxedNode>> agentsPlans = new HashMap<>();
		for (Agent agent : space.domain.agents)
			agentsPlans.put(agent, getAgentSteps(agent, space));

		return RelaxedPlanExtractor.GetAllPossiblePGEPlans(space, space.goal, agentsPlans, onlyExploreAuthorGoals);
	}

	public static HashSet<RelaxedNode> getAgentSteps(Agent agent, SearchSpace space) {
		HashSet<RelaxedNode> agentSteps = new HashSet<>();

		int level = space.graph.size() - 1;
		Expression goals = AgentGoal.get(space.domain, agent);

		for (ConjunctiveClause goal : goals.toDNF().arguments)
			for (PlanGraphLiteralNode node : RelaxedPlanExtractor.getGoalLiterals(space.graph, goal.arguments))
				if (node.getLevel() > 0)
					for (PlanGraphNode step : node.parents)
						if (step instanceof PlanGraphEventNode)
							agentSteps.add(new RelaxedNode((PlanGraphEventNode) step, null, level));

		HashSet<PlanGraphLiteralNode> initialState = RelaxedPlanExtractor.getInitialLiterals(space.graph);
		HashSet<PlanGraphLiteralNode> preconditions = RelaxedPlanExtractor.GetAllPreconditions(agentSteps);

		for (int i = level - 1; i > 0; i--) {
			HashSet<RelaxedNode> newSteps = new HashSet<>();
			for (PlanGraphLiteralNode node : preconditions) {
				for (PlanGraphNode step : node.parents)
					if (step instanceof PlanGraphEventNode) {
						RelaxedNode newRelaxedNode = new RelaxedNode((PlanGraphEventNode) step, null, i);
						agentSteps.add(newRelaxedNode);
						newSteps.add(newRelaxedNode);
					}
			}
			preconditions = RelaxedPlanExtractor.GetAllPreconditions(newSteps);
			for (PlanGraphLiteralNode initialLiteral : initialState)
				if (preconditions.contains(initialLiteral))
					preconditions.remove(initialLiteral);
		}

		return agentSteps;

	}
}
