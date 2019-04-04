package qa;

import java.util.HashSet;
import java.util.Set;

import sabre.Action;
import sabre.Plan;

public class Jaccard {

	static <E> float get(Set<E> a, Set<E> b) {
		HashSet<E> intersection = new HashSet<>();
		HashSet<E> union = new HashSet<>();
		union.addAll(a);
		union.addAll(b);
		for (E item : a)
			if (b.contains(item))
				intersection.add(item);
		return 1 - (float) intersection.size() / union.size();
	}

	static float getAction(Plan a, Plan b) {
		HashSet<Action> set_a = new HashSet<>();
		HashSet<Action> set_b = new HashSet<>();
	
		for (Action action : a)
			set_a.add(action);
	
		for (Action action : b)
			set_b.add(action);
	
		return get(set_a, set_b);
	}

}
