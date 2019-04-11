package qa;

import java.util.ArrayList;
import java.util.Arrays;

import sabre.Action;
import sabre.space.SearchSpace;

public class Convert {
	public static Action stringToAction(String inputString, SearchSpace space) {
		for (Action action : space.actions)
			if (action.toString().equals(inputString))
				return action;
		
		return null;
	}
	
	public static ArrayList<Action> stringToActions(String inputString, SearchSpace space) {
		ArrayList<String> actionStrings = new ArrayList<>(Arrays.asList(inputString.split("&")));
		
		for (int i = actionStrings.size() - 1; i >= 0; i--) {
			String newActionString = actionStrings.get(i).trim();
			actionStrings.remove(i);
			if (!newActionString.isEmpty())
				actionStrings.add(i, newActionString);
		}
		
		ArrayList<Action> actions = new ArrayList<>();
		for (String actionString : actionStrings)
			for (Action action : space.actions)
				if (action.toString().equals(actionString))
				{
					actions.add(action);
					break;
				}
		
		return actions;
	}
}
