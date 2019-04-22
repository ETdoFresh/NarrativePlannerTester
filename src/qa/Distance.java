package qa;

import java.util.HashSet;
import java.util.Set;

import sabre.Action;
import sabre.Event;
import sabre.Plan;
import sabre.space.SearchSpace;

public class Distance {

	static float isifDistance(RelaxedPlan a, RelaxedPlan b, SearchSpace space) {
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
	
	static float actionDistance(Plan a, Plan b) {
		HashSet<Action> set_a = new HashSet<>();
		HashSet<Action> set_b = new HashSet<>();
	
		for (Action action : a)
			set_a.add(action);
	
		for (Action action : b)
			set_b.add(action);
	
		return jaccard(set_a, set_b);
	}

	static <E> float jaccard(Set<E> a, Set<E> b) {
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
