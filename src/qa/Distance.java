package qa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import sabre.Event;
import sabre.space.SearchSpace;

enum DistanceMetric {
	ACTION, ISIF, AGENT_STEP, SCHEMA, AGENT_SCHEMA, GOAL, AGENT_GOAL, AGENT_GOAL_SCHEMA, SATSTEP_GOAL, STEP_LEVEL,
	SATSTEP_GOAL_AGENT_SCHEMA_MULTI, SATSTEP_SCHEMA_GOAL, SATSTEP_GOAL_SCHEMA_MULTI, SATSTEP_SCHEMA_ACTION, TEST, 
	SATSTEP_GOAL_PAIR_SCHEMAS_WEIGTHED, FULL_ACTION, FULL_SATSTEP_GOAL, FULL_SATSTEP_SCHEMA_GOAL, FULL_SATSTEP_GOAL_AGENT_SCHEMA_MULTI,
	FULL_SATSTEP_GOAL_SCHEMA_MULTI
};

public class Distance {

	protected DistanceMetric distanceMetric;
	private SearchSpace space;
	private float[] max;

	public Distance(DistanceMetric metric, SearchSpace space) {
		this.distanceMetric = metric;
		this.space = space;
	}
	
	public boolean isEqualTo(RelaxedPlan a, RelaxedPlan b) {
		switch(distanceMetric) {
		case ACTION:
			break;
		case AGENT_GOAL:			
			break;
		case AGENT_GOAL_SCHEMA:
			break;
		case AGENT_SCHEMA:
			break;
		case AGENT_STEP:
			break;
		case FULL_ACTION:
			break;
		case FULL_SATSTEP_GOAL:
			return fullSatStepGoalEquals(a, b);
		case FULL_SATSTEP_GOAL_AGENT_SCHEMA_MULTI:
			break;
		case FULL_SATSTEP_GOAL_SCHEMA_MULTI:
			break;
		case FULL_SATSTEP_SCHEMA_GOAL:
			return fullSatStepSchemaGoalEquals(a, b);
		case GOAL:
			break;
		case ISIF:
			break;
		case SATSTEP_GOAL:
			return satStepGoalEquals(a, b);
		case SATSTEP_GOAL_PAIR_SCHEMAS_WEIGTHED:
			return satStepGoalPairSchemasWeightedEquals(a, b);
		case SATSTEP_GOAL_AGENT_SCHEMA_MULTI:
			return satStepGoalAgentSchemaMultiEquals(a, b);	
		case SATSTEP_GOAL_SCHEMA_MULTI:
			return satStepGoalSchemaMultiEquals(a, b);
		case SATSTEP_SCHEMA_ACTION:
			return satStepSchemaActionEquals(a, b);
		case SATSTEP_SCHEMA_GOAL:
			return satStepSchemaGoalEquals(a, b);
		case SCHEMA:
			break;
		case STEP_LEVEL:
			break;
		case TEST:
			break;
		default:
			System.out.println("?! What distance metric is this? " + distanceMetric);
			System.exit(1);
		}
		return false;
	}

	private boolean satStepSchemaGoalEquals(RelaxedPlan a, RelaxedPlan b) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean satStepSchemaActionEquals(RelaxedPlan a, RelaxedPlan b) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean satStepGoalSchemaMultiEquals(RelaxedPlan a, RelaxedPlan b) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean satStepGoalAgentSchemaMultiEquals(RelaxedPlan a, RelaxedPlan b) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean satStepGoalPairSchemasWeightedEquals(RelaxedPlan a, RelaxedPlan b) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean satStepGoalEquals(RelaxedPlan a, RelaxedPlan b) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean fullSatStepSchemaGoalEquals(RelaxedPlan a, RelaxedPlan b) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean fullSatStepGoalEquals(RelaxedPlan a, RelaxedPlan b) {
		// TODO Auto-generated method stub
		return false;
	}

	public float getDistance(RelaxedPlan a, RelaxedPlan b) {
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
		case FULL_ACTION:
			dist = fullActionDistance(a, b);
			break;
		case FULL_SATSTEP_GOAL:
			dist = fullSatStepGoalDistance(a, b);
			break;
		case FULL_SATSTEP_GOAL_AGENT_SCHEMA_MULTI:
			dist = fullSatStepGoalAgentSchemaMultiDistance(a, b);
			break;
		case FULL_SATSTEP_GOAL_SCHEMA_MULTI:
			dist = fullSatStepGoalSchemaMultiDistance(a, b);
			break;
		case FULL_SATSTEP_SCHEMA_GOAL:
			dist = fullSatStepSchemaGoalDistance(a, b);
			break;
		case GOAL:
			dist = goalDistance(a, b);
			break;
		case ISIF:
			dist = isifDistance(a, b);
			break;
		case SATSTEP_GOAL:
			dist = satStepGoalDistance(a, b);
			break;
		case SATSTEP_GOAL_PAIR_SCHEMAS_WEIGTHED: // *
			dist = satStepSchemaGoalSchemasWeighted(a, b);
			break;
		case SATSTEP_GOAL_AGENT_SCHEMA_MULTI:
			dist = satStepGoalAgentSchemaMultiDistance(a, b);
			break;
		case SATSTEP_GOAL_SCHEMA_MULTI:
			dist = satStepGoalSchemaMultiDistance(a, b);
			break;
		case SATSTEP_SCHEMA_ACTION:
			dist = satStepSchemaActionDistance(a, b);
			break;
		case SATSTEP_SCHEMA_GOAL: 
			dist = satStepSchemaGoalDistance(a, b);
			break;
		case SCHEMA:
			dist = schemaDistance(a, b);
			break;
		case STEP_LEVEL:
			dist = stepLevelDistance(a, b);
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
	
	/** goals served, satisfying step schemas */
	private float testDistance(RelaxedPlan a, RelaxedPlan b) {
		return combinedJaccard(intersectionOverUnion(a.getGoalsServed(), b.getGoalsServed()),
				intersectionOverUnion(SSSGPair.GetByPlan(a), SSSGPair.GetByPlan(b)));
	}

	/** sat step schema, all grounded actions */
	private float satStepSchemaActionDistance(RelaxedPlan a, RelaxedPlan b) {
		return combinedJaccard(intersectionOverUnion(SSSGPair.GetByPlan(a), SSSGPair.GetByPlan(b)), 
				intersectionOverUnion(a.getActions(), b.getActions()));
	}

	/** grounded sat step, all schemas */
	private float satStepGoalSchemaMultiDistance(RelaxedPlan a, RelaxedPlan b) {
		return combinedJaccard(intersectionOverUnion(a.getSchemas(), b.getSchemas()), 
				intersectionOverUnion(a.getSSGPairs(), b.getSSGPairs()));
	}
	
	public float satStepSchemaGoalSchemasWeighted(RelaxedPlan a, RelaxedPlan b) {
//		return 0.99f * satStepSchemaGoalDistance(a, b) + 
//				0.01f * satStepGoalDistance(a, b) +
//				0.00f * agentSchemaDistance(a, b);
//		HashSet<SSGPair> all = DomainSet.getAllSSGPairs();
//		HashSet<SSGPair> commonAndUnused = difference(all, union(a.getSSGPairs(), b.getSSGPairs()));
//		commonAndUnused = union(all, intersection(a.getSSGPairs(), b.getSSGPairs()));
		return fullSatStepGoalDistance(a,b);
	}

	private float satStepSchemaGoalDistance(RelaxedPlan a, RelaxedPlan b) {
		return jaccard(SSSGPair.GetByPlan(a), SSSGPair.GetByPlan(b));
	}

	private float agentGoalSchemaDistance(RelaxedPlan a, RelaxedPlan b) {
		return jaccard(AGSSSTriple.GetByPlan(a), AGSSSTriple.GetByPlan(b));
	}

	private float agentGoalDistance(RelaxedPlan a, RelaxedPlan b) {
		return jaccard(AGPair.GetByPlan(a), AGPair.GetByPlan(b));
	}

	private float goalDistance(RelaxedPlan a, RelaxedPlan b) {
		return jaccard(a.getGoalsServed(), b.getGoalsServed());
	}

	private float satStepGoalAgentSchemaMultiDistance(RelaxedPlan a, RelaxedPlan b) {
		return combinedJaccard(intersectionOverUnion(a.getSSGPairs(), b.getSSGPairs()), 
				intersectionOverUnion(a.getAgentSchemaPairs(), b.getAgentSchemaPairs()));
	}
	
	private float schemaDistance(RelaxedPlan a, RelaxedPlan b) {
		return jaccard(a.getSchemas(), b.getSchemas());
	}

	private float agentSchemaDistance(RelaxedPlan a, RelaxedPlan b) {
		return jaccard(a.getAgentSchemaPairs(), b.getAgentSchemaPairs());
	}
	
	private float fullActionDistance(RelaxedPlan a, RelaxedPlan b) {
		return fullJaccard(a.getActions(), b.getActions(), DomainSet.getAllActions());
	}
	
	private float actionDistance(RelaxedPlan a, RelaxedPlan b) {
		return jaccard(a.getActions(), b.getActions());
	}
	
	private float fullSatStepGoalAgentSchemaMultiDistance(RelaxedPlan a, RelaxedPlan b) {
		return fullCombinedJaccard(fullIntersectionOverUnion(a.getSSGPairs(), b.getSSGPairs(), DomainSet.getAllSSGPairs()),
				fullIntersectionOverUnion(a.getAgentSchemaPairs(), b.getAgentSchemaPairs(), DomainSet.getAllAgentSchemaPairs()));
	}
	
	private float fullSatStepGoalSchemaMultiDistance(RelaxedPlan a, RelaxedPlan b) {
		return fullCombinedJaccard(fullIntersectionOverUnion(a.getSchemas(), b.getSchemas(), DomainSet.getAllSchemas()),
				fullIntersectionOverUnion(a.getSSGPairs(), b.getSSGPairs(), DomainSet.getAllSSGPairs()));
	}
	
	private float fullSatStepSchemaGoalDistance(RelaxedPlan a, RelaxedPlan b) {
		return fullJaccard(SSSGPair.GetByPlan(a), SSSGPair.GetByPlan(b), DomainSet.getAllSSSGPairs());
	}

	private float fullSatStepGoalDistance(RelaxedPlan a, RelaxedPlan b) {
		return fullJaccard(a.getSSGPairs(), b.getSSGPairs(), DomainSet.getAllSSGPairs());
	}
	
	public float satStepGoalDistance(RelaxedPlan a, RelaxedPlan b) {
		return jaccard(a.getSSGPairs(), b.getSSGPairs());
	}

	public float stepLevelDistance(RelaxedPlan a, RelaxedPlan b) {
		return jaccard(SLPair.GetByPlan(a), SLPair.GetByPlan(b));
	}

	/** Agent step distance between two plans: Euclidean square of the agent step vectors */
	private float agentStepDistance(RelaxedPlan a, RelaxedPlan b) {
		float[] vectorA = Vector.getAgentStepVector(space, a);
		float[] vectorB = Vector.getAgentStepVector(space, b);
		// vectorA = Vector.divide(vectorA, a.size());
		// vectorB = Vector.divide(vectorB, b.size());
		vectorA = Vector.normalize(vectorA);
		vectorB = Vector.normalize(vectorB);
		return Vector.distance(vectorA, vectorB);
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
	
	private HashSet<Event> getExplSummaries(ArrayList<Explanation> explanations) {
		HashSet<Event> summaries = new HashSet<>();
		for (Explanation e : explanations)
			summaries.addAll(e.getSatisfyingSteps());
		return summaries;
	}

	/** Jaccard distance between two sets: 1 - intersection over union **/
	private <E> float jaccard(Set<E> a, Set<E> b) {
		return 1f - intersectionOverUnion(a, b);
	}
	
	private float fullCombinedJaccard(float a, float b) {
		return (a+b)/2f;
	}

	private float combinedJaccard(float a, float b) {
		return 1f - ((a+b)/2);
	}
	
	private <E> float intersectionOverUnion(Set<E> a, Set<E> b) {
		HashSet<E> intersection = intersection(a, b);
		HashSet<E> union = union(a, b);
		if (union.isEmpty())
			return 0f;
		return (float) intersection.size() / union.size();
	}
	
	// Testing: Not 1- , just int/un
	private <E> float fullJaccard(Set<E> a, Set<E> b, Set<E> all) {
		return fullIntersectionOverUnion(a, b, all);
	}
	
	private <E> float fullIntersectionOverUnion(Set<E> a, Set<E> b, Set<E> all) {
		HashSet<E> fullIntersection = fullIntersection(a, b, all);
		if(all.isEmpty())
			return 0;
		return (float) fullIntersection.size() / all.size();
	}

	private <E> HashSet<E> union(Set<E> a, Set<E> b){
		HashSet<E> union = new HashSet<>();
		union.addAll(a);
		union.addAll(b);
		return union;
	}
	
	private <E> HashSet<E> intersection(Set<E> a, Set<E> b){
		HashSet<E> intersection = new HashSet<>();
		for (E item : a)
			if (b.contains(item))
				intersection.add(item);	
		return intersection;
	}
	
	private <E> HashSet<E> fullIntersection(Set<E> a, Set<E> b, Set<E> all) {
		HashSet<E> fullIntersection = new HashSet<>();
		fullIntersection.addAll(all);
		fullIntersection.removeAll(union(a, b));
		fullIntersection.addAll(intersection(a, b));
		return fullIntersection;
	}
	
}
