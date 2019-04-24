package qa;

import java.util.ArrayList;

import sabre.Action;
import sabre.Agent;
import sabre.space.SearchSpace;
import sabre.util.ImmutableSet;

public class Vector {

	/** The number of steps taken by each agent */
	public static int[] getAgentStepVector(SearchSpace space, RelaxedPlan plan) {
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

	/** The number of times each type of action appears in the plan */
	public static int[] getSchemaVector(SearchSpace space, RelaxedPlan plan) {
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

	/** The number of times each agent takes an action of each type */
	public static int[] getAgentSchemaVector(SearchSpace space, RelaxedPlan plan) {
		ArrayList<String> actionNames = new ArrayList<>();
		for (Action action : space.domain.actions)
			if (!actionNames.contains(action.name))
				actionNames.add(action.name);

		ImmutableSet<Agent> agents = space.domain.agents;
		int[] vector = new int[actionNames.size() * agents.size()];

		// ActionNames=i, Agents=j
		for (int i = 0; i < actionNames.size(); i++)
			for (int j = 0; j < agents.size(); j++)
				for (RelaxedNode node : plan)
					if (node.eventNode.event.name.equals(actionNames.get(i)))
						if (node.eventNode.event instanceof Action) {
							Action action = (Action) node.eventNode.event;
							if (action.agents.contains(agents.get(j)))
								vector[i * agents.size() + j]++;
						}
		return vector;
	}
}
