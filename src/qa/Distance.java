package qa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import sabre.Action;
import sabre.Event;
import sabre.Plan;
import sabre.logic.Literal;
import sabre.space.SearchSpace;

enum DistanceMetric {
	ACTION, ISIF, AGENT_STEP, SCHEMA, AGENT_SCHEMA, GOAL, AGENT_GOAL, AGENT_GOAL_SCHEMA, SATSTEP_GOAL_PAIR, STEP_LEVEL,
	SSG_SCHEMA_MULTI, SSSCHEMA_GOAL
};

public class Distance {

	protected DistanceMetric distanceMetric;
	private SearchSpace space;
	private float[] max;

	public Distance(DistanceMetric metric, SearchSpace space) {
		this.distanceMetric = metric;
		this.space = space;
	}

	public float getDistance(RelaxedPlan a, RelaxedPlan b, ArrayList<RelaxedPlan> plans) {
		float dist = -1;
		switch (distanceMetric) {
		case ACTION:
			dist = actionDistance(a, b);
			break;
		case ISIF:
			dist = isifDistance(a, b);
			break;
		case AGENT_STEP:
			dist = agentStepDistance(a, b);
			break;
		case SCHEMA:
			dist = schemaDistance(a, b);
			break;
		case AGENT_SCHEMA:
			if (max == null)
				max = getAgentSchemaMaxVector(plans);
			dist = agentSchemaDistance(a, b, max);
			break;
		case GOAL:
			dist = goalDistance(a, b);
			break;
		case SSSCHEMA_GOAL:
			dist = goalSchemaDistance(a, b);
			break;
		case AGENT_GOAL:
			dist = agentGoalDistance(a, b);
			break;
		case AGENT_GOAL_SCHEMA:
			dist = agentGoalSchemaDistance(a, b);
			break;
		case SATSTEP_GOAL_PAIR:
			dist = SSGPairDistance(a, b);
			break;
		case STEP_LEVEL:
			dist = StepLevel(a, b);
			break;
		case SSG_SCHEMA_MULTI:
			dist = schemaSSGMultiDistance(a, b);
			break;
		default:
			System.out.println("?! What distance metric is this? " + distanceMetric);
			System.exit(1);
		}
		return dist;
	}

	/**
	 * @param plans - All Relaxed Plans
	 * @return AgentSchema Vector where each dimension = MAX_OCCURENCES of dimension
	 */
	private float[] getAgentSchemaMaxVector(ArrayList<RelaxedPlan> plans) {
		float[] max = new float[0];
		if (plans.size() > 0)
			max = Vector.getAgentSchemaVector(space, plans.get(0));

		// Prevent Divide by 0
		for (int i = 0; i < max.length; i++)
			max[i] = 1;

		// Get Max!
		for (int i = 0; i < plans.size(); i++) {
			float[] vector = Vector.getAgentSchemaVector(space, plans.get(i));
			for (int j = 0; j < vector.length; j++)
				if (max[j] < vector[j])
					max[j] = vector[j];
		}
		return max;
	}

	private float goalSchemaDistance(RelaxedPlan a, RelaxedPlan b) {
		HashSet<SSSGPair> goalSetA = SSSGPair.GetByPlan(a);
		HashSet<SSSGPair> goalSetB = SSSGPair.GetByPlan(b);
		return jaccard(goalSetA, goalSetB);
	}

	private float agentGoalSchemaDistance(RelaxedPlan a, RelaxedPlan b) {
		HashSet<AGSSSTriple> goalSetA = AGSSSTriple.GetByPlan(a);
		HashSet<AGSSSTriple> goalSetB = AGSSSTriple.GetByPlan(b);
		return jaccard(goalSetA, goalSetB);
	}

	private float agentGoalDistance(RelaxedPlan a, RelaxedPlan b) {
		HashSet<AGPair> goalSetA = AGPair.GetByPlan(a);
		HashSet<AGPair> goalSetB = AGPair.GetByPlan(b);
		return jaccard(goalSetA, goalSetB);
	}

	private float goalDistance(RelaxedPlan a, RelaxedPlan b) {
		HashSet<Literal> goalSetA = new HashSet<>();
		HashSet<Literal> goalSetB = new HashSet<>();
		for (RelaxedNode node : a)
			goalSetA.addAll(node.inServiceOfGoalLiteral);
		for (RelaxedNode node : b)
			goalSetB.addAll(node.inServiceOfGoalLiteral);
		return jaccard(goalSetA, goalSetB);
	}

	private float schemaSSGMultiDistance(RelaxedPlan a, RelaxedPlan b) {
		HashSet<SSGPair> setA = a.getSSGPairs();
		HashSet<SSGPair> setB = b.getSSGPairs();
		HashSet<AgentSchemaPair> setA2 = a.getAgentSchemaPairs();
		HashSet<AgentSchemaPair> setB2 = b.getAgentSchemaPairs();
		return 1 - (0.5f * (jaccard(setA, setB) + jaccard(setA2, setB2)));
	}

	/**
	 * ISIF distance between two plans: Weighted Jaccards of the sets of important
	 * steps and explanation summaries
	 */
	private float isifDistance(RelaxedPlan a, RelaxedPlan b) {
		Set<Event> importantSteps_a = new HashSet<>();
		Set<Event> importantSteps_b = new HashSet<>();

		for (RelaxedNode step : a.importantSteps)
			importantSteps_a.add(step.eventNode.event);
		for (RelaxedNode step : b.importantSteps)
			importantSteps_b.add(step.eventNode.event);

		Set<Event> explSummaries_a = getExplSummaries(a.explanations);
		Set<Event> explSummaries_b = getExplSummaries(b.explanations);
		// System.out.println("Jaccard of IF summaries: " + jaccard(explSummaries_a,
		// explSummaries_b)+"\n");
		float returnVal = 1
				- (0.5f * (jaccard(importantSteps_a, importantSteps_b) + jaccard(explSummaries_a, explSummaries_b)));
		return returnVal;
	}

	private HashSet<Event> getExplSummaries(ArrayList<Explanation> explanations) {
		HashSet<Event> summaries = new HashSet<>();
		for (Explanation e : explanations)
			summaries.addAll(e.getSatisfyingSteps());
		return summaries;
	}

	/**
	 * Agent step distance between two plans: Euclidean square of the agent step
	 * vectors
	 */
	private float agentStepDistance(RelaxedPlan a, RelaxedPlan b) {
		float[] vectorA = Vector.getAgentStepVector(space, a);
		float[] vectorB = Vector.getAgentStepVector(space, b);
		// vectorA = Vector.divide(vectorA, a.size());
		// vectorB = Vector.divide(vectorB, b.size());
		vectorA = Vector.normalize(vectorA);
		vectorB = Vector.normalize(vectorB);
		return Vector.distance(vectorA, vectorB);
	}

	/** Schema distance between two plans: Euclidean square of the schema vectors */
	private float schemaDistance(RelaxedPlan a, RelaxedPlan b) {
		float[] vectorA = Vector.getSchemaVector(space, a);
		float[] vectorB = Vector.getSchemaVector(space, b);
		// vectorA = Vector.divide(vectorA, a.size());
		// vectorB = Vector.divide(vectorB, b.size());
		vectorA = Vector.normalize(vectorA);
		vectorB = Vector.normalize(vectorB);
		return Vector.distance(vectorA, vectorB);
	}

	/**
	 * Agent schema distance bewteen two plans: Euclidean square of the agent schema
	 * vectors
	 * 
	 * @param allPlans
	 */
	private float agentSchemaDistance(RelaxedPlan a, RelaxedPlan b, float[] max) {
		float[] vectorA = Vector.getAgentSchemaVector(space, a);
		float[] vectorB = Vector.getAgentSchemaVector(space, b);
		vectorA = Vector.divideComponentWise(vectorA, max);
		vectorB = Vector.divideComponentWise(vectorB, max);
		// vectorA = Vector.divide(vectorA, a.size());
		// vectorB = Vector.divide(vectorB, b.size());
		// vectorA = Vector.normalize(vectorA);
		// vectorB = Vector.normalize(vectorB);
		return Vector.distance(vectorA, vectorB);
	}

	/**
	 * Action distance between two plans: Jaccard of the sets of actions in each
	 * plan
	 */
	private float actionDistance(RelaxedPlan a, RelaxedPlan b) {
		HashSet<Event> set_a = new HashSet<>();
		HashSet<Event> set_b = new HashSet<>();
		for (RelaxedNode action : a)
			set_a.add(action.eventNode.event);
		for (RelaxedNode action : b)
			set_b.add(action.eventNode.event);
		return jaccard(set_a, set_b);
	}

	private float actionDistance(Plan a, Plan b) {
		HashSet<Action> set_a = new HashSet<>();
		HashSet<Action> set_b = new HashSet<>();
		for (Action action : a)
			set_a.add(action);
		for (Action action : b)
			set_b.add(action);
		return jaccard(set_a, set_b);
	}

	/** Jaccard distance between two sets: intersection over union **/
	private <E> float jaccard(Set<E> a, Set<E> b) {
		HashSet<E> intersection = new HashSet<>();
		HashSet<E> union = new HashSet<>();
		union.addAll(a);
		union.addAll(b);
		for (E item : a)
			if (b.contains(item))
				intersection.add(item);
		if (union.isEmpty())
			return 0f;
		return 1 - (float) intersection.size() / union.size();
	}

	public float SSGPairDistance(RelaxedPlan a, RelaxedPlan b) {
		HashSet<SSGPair> setA = a.getSSGPairs();
		HashSet<SSGPair> setB = b.getSSGPairs();
		return jaccard(setA, setB);
	}

	public float StepLevel(RelaxedPlan a, RelaxedPlan b) {
		HashSet<SLPair> goalSetA = SLPair.GetByPlan(a);
		HashSet<SLPair> goalSetB = SLPair.GetByPlan(b);
		return jaccard(goalSetA, goalSetB);
	}
}
