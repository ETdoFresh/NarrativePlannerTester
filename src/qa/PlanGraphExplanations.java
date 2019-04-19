package qa;

import java.util.ArrayList;
import java.util.HashMap;

import sabre.Agent;
import sabre.graph.PlanGraph;
import sabre.space.SearchSpace;

public class PlanGraphExplanations {
	public static ArrayList<RelaxedPlan> getExplainedPlans(SearchSpace space) {
		ArrayList<RelaxedPlan> classicSolution = RelaxedPlanExtractor.GetAllPossibleClassicalPlans(space, space.goal);
		HashMap<Agent, ArrayList<RelaxedPlan>> agentsPlans = new HashMap<>();
		for(Agent agent : space.domain.agents)
			agentsPlans.put(agent, RelaxedPlanExtractor.GetAllPossibleClassicalPlans(space, AgentGoal.get(space.domain, agent)));
		
		return RelaxedPlanExtractor.GetAllPossiblePGEPlans(space, space.goal, agentsPlans);
	}
}
