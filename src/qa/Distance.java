package qa;

import java.util.HashSet;
import java.util.Set;

import sabre.Action;
import sabre.Event;
import sabre.Plan;
import sabre.graph.PlanGraphEventNode;

public class Distance {

	static float isifDistance(RelaxedPlan a, RelaxedPlan b) {
		Set<Event> E_a = new HashSet<>();
		Set<Event> E_b = new HashSet<>();

		// TODO: Only add important steps
		for(PlanGraphEventNode step : a) {
			E_a.add(step.event);			
		}
		for(PlanGraphEventNode step : b) {
			E_b.add(step.event);
		}

		// TODO: Add intention frame summaries
		Set<Object> J_a = new HashSet<>();
		Set<Object> J_b = new HashSet<>();
		
		return 1 - 0.5f * (jaccard(E_a, E_b) + jaccard(J_a, J_b));
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
