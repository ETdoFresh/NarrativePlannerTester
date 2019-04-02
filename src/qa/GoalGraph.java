package qa;

import java.util.ArrayList;

import sabre.Action;
import sabre.Agent;
import sabre.logic.Assignment;
import sabre.logic.Expression;
import sabre.space.SearchSpace;

public class GoalGraph {
	public Agent agent;
	public Assignment agentGoal;
	public ArrayList<GoalGraphNode> nodes = new ArrayList<>();

	public GoalGraph(Agent agent, Assignment agentGoal) {
		this.agent = agent;
		this.agentGoal = agentGoal;
	}

	// TODO Implement Correctly... still quite a way off
	public boolean extend(SearchSpace space) {
		int nodeSize = nodes.size();
		if (nodeSize == 0) {
			for (Action action : space.actions)
				action.effect.toDNF().arguments.get(0);
			return nodes.size() > 0;
		}
		else {
			return nodes.size() != nodeSize;
		}
	}

	@Override
	public String toString() {
		return "GoalGraph: " + agent + ", " + agentGoal;
	}
}