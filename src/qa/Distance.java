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
	SSG_AGENT_SCHEMA_MULTI, SSSCHEMA_GOAL, SSG_SCHEMA_MULTI, TEST
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
		case AGENT_GOAL:
			dist = agentGoalDistance(a, b);
			break;
		case AGENT_GOAL_SCHEMA:
			dist = agentGoalSchemaDistance(a, b);
			break;
		case AGENT_SCHEMA:
			dist = agentSchemaDistance(a, b);
			break;
		case AGENT_STEP:
			dist = agentStepDistance(a, b);
			break;
		case GOAL:
			dist = goalDistance(a, b);
			break;
		case ISIF:
			dist = isifDistance(a, b);
			break;
		case SATSTEP_GOAL_PAIR:
			dist = SSGPairDistance(a, b);
			break;
		case SCHEMA:
			dist = schemaDistance(a, b);
			break;
		case SSG_AGENT_SCHEMA_MULTI:
			dist = schemaSSGMultiDistance(a, b);
			break;
		case SSG_SCHEMA_MULTI:
			dist = ssgSchemaMultiDistance(a, b);
			break;
		case SSSCHEMA_GOAL:
			dist = goalSchemaDistance(a, b);
			break;
		case STEP_LEVEL:
			dist = StepLevel(a, b);
			break;
		case TEST:
			dist = testDistance(a, b);
			break;
		default:
			System.out.println("?! What distance metric is this? " + distanceMetric);
			System.exit(1);
		}
		return dist;
	}

	private float testDistance(RelaxedPlan a, RelaxedPlan b) {
		return 0;
	}

	private float ssgSchemaMultiDistance(RelaxedPlan a, RelaxedPlan b) {
		return combinedJaccard(intersectionOverUnion(a.getSchemas(), b.getSchemas()), 
				intersectionOverUnion(a.getSSGPairs(), b.getSSGPairs()));
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
		return combinedJaccard(
				intersectionOverUnion(a.getSSGPairs(), b.getSSGPairs()), 
				intersectionOverUnion(a.getAgentSchemaPairs(), b.getAgentSchemaPairs()));
	}
	
	private float combinedJaccard(float a, float b) {
		return 1f - ((a+b)/2);
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

	private float schemaDistance(RelaxedPlan a, RelaxedPlan b) {
		return jaccard(a.getSchemas(), b.getSchemas());
	}

	private float agentSchemaDistance(RelaxedPlan a, RelaxedPlan b) {
		return jaccard(a.getAgentSchemaPairs(), b.getAgentSchemaPairs());
	}

	private float actionDistance(RelaxedPlan a, RelaxedPlan b) {
		HashSet<Event> set_a = new HashSet<>();
		HashSet<Event> set_b = new HashSet<>();
		for (RelaxedNode action : a)
			set_a.add(action.eventNode.event);
		for (RelaxedNode action : b)
			set_b.add(action.eventNode.event);
		return jaccard(set_a, set_b);
	}

	public float SSGPairDistance(RelaxedPlan a, RelaxedPlan b) {
		return jaccard(a.getSSGPairs(), b.getSSGPairs());
	}

	public float StepLevel(RelaxedPlan a, RelaxedPlan b) {
		return jaccard(SLPair.GetByPlan(a), SLPair.GetByPlan(b));
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
		return combinedJaccard(intersectionOverUnion(importantSteps_a, importantSteps_b), 
				intersectionOverUnion(explSummaries_a, explSummaries_b));
	}

	/** Jaccard distance between two sets: 1 - intersection over union **/
	private <E> float jaccard(Set<E> a, Set<E> b) {
		return 1f - intersectionOverUnion(a, b);
	}

	/** Jaccard is 1 - this */
	private <E> float intersectionOverUnion(Set<E> a, Set<E> b) {
		HashSet<E> intersection = new HashSet<>();
		HashSet<E> union = new HashSet<>();
		union.addAll(a);
		union.addAll(b);
		for (E item : a)
			if (b.contains(item))
				intersection.add(item);
		if (union.isEmpty())
			return 0f;
		return (float) intersection.size() / union.size();
	}


}
