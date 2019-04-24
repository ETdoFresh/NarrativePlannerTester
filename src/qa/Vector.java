package qa;

import java.util.ArrayList;

import sabre.Action;
import sabre.Agent;
import sabre.space.SearchSpace;

public class Vector {
	public static int[] getAgentStep(SearchSpace space, RelaxedPlan plan) {
		int[] vector = new int[space.domain.agents.size()];
		for (int i = 0; i < space.domain.agents.size(); i++) {
			Agent agent = space.domain.agents.get(i);
			for (RelaxedNode node : plan) {
				if (node.eventNode.event instanceof Action) {
					Action action = (Action) node.eventNode.event;
					if (action.agents.contains(agent))
						vector[i]++;
				}
			}
		}
		return vector;
	}

	public static int[] getActionSchema(SearchSpace space, RelaxedPlan plan) {
		ArrayList<String> actionNames = new ArrayList<>();
		for (Action action : space.domain.actions)
			if (!actionNames.contains(action.name))
				actionNames.add(action.name);

		int[] vector = new int[actionNames.size()];
		for (int i = 0; i < actionNames.size(); i++) {
			for (RelaxedNode node : plan) {
				if (node.eventNode.event.name.equals(actionNames.get(i)))
					vector[i]++;
			}
		}
		return vector;
	}

}
