package qa;

import java.util.ArrayList;

import sabre.Action;
import sabre.graph.PlanGraphActionNode;
import sabre.graph.PlanGraphEventNode;
import sabre.space.SearchSpace;

public class Landmark {

	public static ArrayList<RelaxedPlan> filter(ArrayList<RelaxedPlan> relaxedPlans, String landmark, SearchSpace space) {
		ArrayList<Action> landmarkSteps = Convert.stringToActions(landmark, space);
		ArrayList<RelaxedPlan> filteredList = new ArrayList<>(relaxedPlans);
		for (int i = filteredList.size() - 1; i >= 0; i--)
		{
			RelaxedPlan relaxedPlan = filteredList.get(i);
			if (!containsAtLeastOneStep(relaxedPlan, landmarkSteps))
				filteredList.remove(i);
		}
		return filteredList;
	}

	private static boolean containsAtLeastOneStep(RelaxedPlan relaxedPlan, ArrayList<Action> landmarkSteps) {
		for(Action action : landmarkSteps)
			for (PlanGraphEventNode relaxedAction : relaxedPlan)
				if (action.equals(relaxedAction.event))
					return true;
		
		return false;
	}

}
