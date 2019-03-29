package qa;

import java.util.ArrayList;
import java.util.Iterator;

import sabre.Action;
import sabre.Plan;
import sabre.graph.PlanGraphActionNode;

public class RelaxedPlan implements Iterable<PlanGraphActionNode> {
	private ArrayList<PlanGraphActionNode> actions = new ArrayList<PlanGraphActionNode>(); 
	
	public RelaxedPlan clone() {
		RelaxedPlan clone = new RelaxedPlan();
		clone.actions.addAll(actions);
		return clone;
	}
	
	public PlanGraphActionNode last() {
		if (size() > 0) return actions.get(size()-1);
		else return null;
	}
	
	public void push(PlanGraphActionNode action) {
		actions.add(0, action);
	}
	
	public int size() {
		return actions.size();
	}
	
	public boolean contains(PlanGraphActionNode o) {
		return actions.contains(o);
	}
	
	public boolean contains(Action action) {
		for(PlanGraphActionNode actionNode : actions)
			if (action.equals(actionNode.event))
				return true;
		
		return false;
	}

	public Iterator<PlanGraphActionNode> iterator() {
		return actions.iterator();
	}
	
	@Override
	public String toString() {
		String str = "";
		for(PlanGraphActionNode action : actions)
			str += Main.BLANK + action + "\n";
		return str;
	}
}
