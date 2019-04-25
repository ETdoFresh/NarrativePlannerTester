package qa;

import java.util.ArrayList;

import sabre.Action;
import sabre.Agent;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Expression;
import sabre.logic.Literal;
import sabre.space.SearchSpace;
import sabre.util.ImmutableSet;

public class Vector {

	/** The number of steps taken by each agent */
	public static float[] getAgentStepVector(SearchSpace space, RelaxedPlan plan) {
		float[] vector = new float[space.domain.agents.size()];
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
	public static float[] getSchemaVector(SearchSpace space, RelaxedPlan plan) {
		ArrayList<String> actionNames = new ArrayList<>();
		for (Action action : space.domain.actions)
			if (!actionNames.contains(action.name))
				actionNames.add(action.name);

		float[] vector = new float[actionNames.size()];
		for (int i = 0; i < actionNames.size(); i++) {
			for (RelaxedNode node : plan) {
				if (node.eventNode.event.name.equals(actionNames.get(i)))
					vector[i]++;
			}
		}
		return vector;
	}

	/** The number of times each agent takes an action of each type */
	public static float[] getAgentSchemaVector(SearchSpace space, RelaxedPlan plan) {
		ArrayList<String> actionNames = new ArrayList<>();
		for (Action action : space.domain.actions)
			if (!actionNames.contains(action.name))
				actionNames.add(action.name);

		ImmutableSet<Agent> agents = space.domain.agents;
		float[] vector = new float[actionNames.size() * agents.size()];

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

	public static float[] getGoalVector(SearchSpace space, RelaxedPlan plan) {
		ArrayList<Literal> goals = new ArrayList<>();

		// Author Goals
		for (ConjunctiveClause goal : space.domain.goal.toDNF().arguments)
			for (Literal literal : goal.arguments)
				if (!goals.contains(literal))
					goals.add(literal);

		// Agent Goals
		for (Expression agentGoal : AgentGoal.getAll(space.domain))
			for (ConjunctiveClause goal : agentGoal.toDNF().arguments)
				for (Literal literal : goal.arguments)
					if (!goals.contains(literal))
						goals.add(literal);

		float[] vector = new float[goals.size()];
		for (int i = 0; i < goals.size(); i++)
		for (RelaxedNode node : plan) {			
			for (ConjunctiveClause effect : node.eventNode.event.effect.toDNF().arguments)
				if(effect.arguments.contains(goals.get(i))) {
					vector[i]++;
					break;
				}
					
		}
		return vector;
	}

	public static float squareMagnitude(float[] vector) {
		float squareMagnitude = 0;
		for (float value : vector)
			squareMagnitude += value * value;
		return squareMagnitude;
	}

	public static float magnitude(float[] vector) {
		return (float) Math.sqrt(squareMagnitude(vector));
	}

	public static float squareDistance(float[] vectorA, float[] vectorB) {
		float[] difference = vectorA.clone();
		for (int i = 0; i < difference.length; i++)
			difference[i] -= vectorB[i];
		return squareMagnitude(difference);
	}

	public static float distance(float[] vectorA, float[] vectorB) {
		return (float) Math.sqrt(squareDistance(vectorA, vectorB));
	}

	public static float[] divide(float[] vector, float divisor) {
		float[] divided = vector.clone();
		for (int i = 0; i < divided.length; i++)
			divided[i] /= divisor;
		return divided;
	}

	public static float[] divideComponentWise(float[] vector, float[] divisor) {
		float[] divided = vector.clone();
		for (int i = 0; i < divided.length; i++)
			divided[i] /= divisor[i];
		return divided;
	}

	public static float[] normalize(float[] vector) {
		return divide(vector, magnitude(vector));
	}
}
