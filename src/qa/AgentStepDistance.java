package qa;

import sabre.Action;
import sabre.Agent;
import sabre.space.SearchSpace;

public class AgentStepDistance {
	public static int[] getVector(SearchSpace space, RelaxedPlan plan) {
		int[] vector = new int[space.domain.agents.size()];
		for(int i = 0; i < space.domain.agents.size(); i++) {
			Agent agent = space.domain.agents.get(i);
			for (RelaxedNode node : plan) {
				if (node.eventNode.event instanceof Action) {
					Action action = (Action)node.eventNode.event;
					if (action.agents.contains(agent))
						vector[i]++;
				}
			}
		}
		return vector;
	}

}
