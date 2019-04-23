package qa;

import java.util.HashSet;
import java.util.Set;

import sabre.Action;
import sabre.Event;
import sabre.Plan;
import sabre.space.SearchSpace;

enum DistanceMetric { ISIF, AgentStep, ActionDistance };

public class Distance {
		
	private DistanceMetric distanceMetric;
	private SearchSpace space;

	public Distance(DistanceMetric metric, SearchSpace space) {
		this.distanceMetric = metric;
		this.space = space;
	}
	
	public float getDistance(RelaxedPlan a, RelaxedPlan b) {
		switch(distanceMetric) {
			case ISIF: 
				return isifDistance(a, b);
			case AgentStep:
				return agentStepDistance(a, b);
			case ActionDistance:
				return actionDistance(a, b);
		}
		System.out.println("?! What distance metric is this? " + distanceMetric);
		System.exit(1);
		return 0;
	}
	
	private float isifDistance(RelaxedPlan a, RelaxedPlan b) {
		Set<Event> importantSteps_a = new HashSet<>();
		Set<Event> importantSteps_b = new HashSet<>();

		for(RelaxedNode step : a.getImportantSteps(space))
			importantSteps_a.add(step.eventNode.event);	
		for(RelaxedNode step : b.getImportantSteps(space))
			importantSteps_b.add(step.eventNode.event);
		
		Set<Event> ifSummaries_a = new HashSet<>();
		Set<Event> ifSummaries_b = new HashSet<>();

		for(Explanation e : a.explanations)
			ifSummaries_a.add(e.steps.lastElement());
		for(Explanation e : b.explanations)
			ifSummaries_b.add(e.steps.lastElement());
		
		return 1 - 0.5f * (jaccard(importantSteps_a, importantSteps_b) + jaccard(ifSummaries_a, ifSummaries_b));
	}
	
	private float agentStepDistance(RelaxedPlan a, RelaxedPlan b) {
		int[] vectorA = AgentStepDistance.getVector(space, a);
		int[] vectorB = AgentStepDistance.getVector(space, b);
		float euclideanSquareDistance = 0;
		for (int i = 0; i < vectorA.length; i++)
			euclideanSquareDistance += (float)Math.pow(vectorA[i] - vectorB[i], 2);
		return euclideanSquareDistance;
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

	private float actionDistance(Plan a, Plan b) {
		HashSet<Action> set_a = new HashSet<>();
		HashSet<Action> set_b = new HashSet<>();
		for (Action action : a)
			set_a.add(action);
		for (Action action : b)
			set_b.add(action);
		return jaccard(set_a, set_b);
	}

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
