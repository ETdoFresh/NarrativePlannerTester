package qa;

import java.util.HashSet;
import java.util.Set;

import sabre.Action;
import sabre.Event;
import sabre.Plan;
import sabre.space.SearchSpace;

enum DistanceMetric { ACTION, ISIF, AGENT_STEP, SCHEMA, AGENT_SCHEMA };

public class Distance {
		
	private DistanceMetric distanceMetric;
	private SearchSpace space;

	public Distance(DistanceMetric metric, SearchSpace space) {
		this.distanceMetric = metric;
		this.space = space;
	}
	
	public float getDistance(RelaxedPlan a, RelaxedPlan b) {
		float dist = -1;
		switch(distanceMetric) {
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
				dist = agentSchemaDistance(a, b);
				break;
			default:
				System.out.println("?! What distance metric is this? " + distanceMetric);
				System.exit(1);
		}
		if(dist == 0)
			return 0.00000001f;
		return dist;
	}
	
	/** ISIF distance between two plans: Weighted Jaccards of the sets of important steps and explanation summaries */
	private float isifDistance(RelaxedPlan a, RelaxedPlan b) {
		Set<Event> importantSteps_a = new HashSet<>();
		Set<Event> importantSteps_b = new HashSet<>();

		for(RelaxedNode step : a.getImportantSteps(space))
			importantSteps_a.add(step.eventNode.event);	
		for(RelaxedNode step : b.getImportantSteps(space))
			importantSteps_b.add(step.eventNode.event);
		
		Set<Event> explSummaries_a = new HashSet<>();
		Set<Event> explSummaries_b = new HashSet<>();

		for(Explanation e : a.explanations)
			explSummaries_a.add(e.steps.lastElement());
		for(Explanation e : b.explanations)
			explSummaries_b.add(e.steps.lastElement());
		
		return 1 - 0.5f * (jaccard(importantSteps_a, importantSteps_b) + jaccard(explSummaries_a, explSummaries_b));
	}

	/** Agent step distance between two plans: Euclidean square of the agent step vectors */
	private float agentStepDistance(RelaxedPlan a, RelaxedPlan b) {
		int[] vectorA = Vector.getAgentStepVector(space, a);
		int[] vectorB = Vector.getAgentStepVector(space, b);
		float euclideanSquareDistance = 0;
		for (int i = 0; i < vectorA.length; i++)
			euclideanSquareDistance += (float)Math.pow(vectorA[i] - vectorB[i], 2);
		return euclideanSquareDistance;
	}

	/** Schema distance between two plans: Euclidean square of the schema vectors */
	private float schemaDistance(RelaxedPlan a, RelaxedPlan b) {
		int[] vectorA = Vector.getSchemaVector(space, a);
		int[] vectorB = Vector.getSchemaVector(space, b);
		float euclideanSquareDistance = 0;
		for (int i = 0; i < vectorA.length; i++)
			euclideanSquareDistance += (float)Math.pow(vectorA[i] - vectorB[i], 2);
		return euclideanSquareDistance;
	}
	
	/** Agent schema distance bewteen two plans: Euclidean square of the agent schema vectors */
	private float agentSchemaDistance(RelaxedPlan a, RelaxedPlan b) {
		int[] vectorA = Vector.getAgentSchemaVector(space, a);
		int[] vectorB = Vector.getAgentSchemaVector(space, b);
		float euclideanSquareDistance = 0;
		for (int i = 0; i < vectorA.length; i++)
			euclideanSquareDistance += (float)Math.pow(vectorA[i] - vectorB[i], 2);
		return euclideanSquareDistance;
	}
		
	/** Action distance between two plans: Jaccard of the sets of actions in each plan */
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
		return 1 - (float) intersection.size() / union.size();
	}


}
