package qa;

import java.util.ArrayList;

public class RelaxedPlanCleaner {
	public static void RemoveNoOps(ArrayList<RelaxedPlan> plans) {
		for (RelaxedPlan plan : plans)
			plan.removeNoOps();
	}

	public static void RemoveDuplicates(ArrayList<RelaxedPlan> plans) {
		for (int i = plans.size() -1; i >= 0; i--)
			for (int j = i - 1; j >= 0; j--)
				if (plans.get(i).equals(plans.get(j))) {
					plans.remove(i);
					break;
				}
	}
}
