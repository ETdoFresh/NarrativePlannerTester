package qa;

import java.util.ArrayList;
import java.util.Iterator;

import sabre.Action;
import sabre.graph.PlanGraphActionNode;

public class RelaxedPlan implements Iterable<PlanGraphActionNode> {
	private ArrayList<PlanGraphActionNode> actions = new ArrayList<PlanGraphActionNode>(); 
	public int clusterAssignment = -1;

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
	
	public PlanGraphActionNode get(int index) {
		return actions.get(index);
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
	
	public float jaccard(RelaxedPlan other) {
		return 1f - this.intersection(other) / this.union(other);
	}
	
	public float intersection(RelaxedPlan other) {
		if(other==null)
			return 0;
		float count = 0;
		for(PlanGraphActionNode action : actions) {
			if(other.contains(action))
				count++;
		}
		return count;
	}
	
	public float union(RelaxedPlan other) {
		if(other==null)
			return size();
		ArrayList<PlanGraphActionNode> union = new ArrayList<>();
		union.addAll(actions);
		for(PlanGraphActionNode action : ((RelaxedPlan)other).actions)
			if(!union.contains(action))
				union.add(action);
		return (float)union.size();
	}
	
	public static RelaxedPlan medoid(ArrayList<RelaxedPlan> plans) {
		RelaxedPlan medoid = null;
		float[] averageDistances = new float[plans.size()];
		for(int i=0; i<plans.size(); i++) {
			float sum = 0;
			for(RelaxedPlan other : plans) {
				sum += plans.get(i).jaccard(other);
			}
			averageDistances[i] = sum / plans.size();
		}
		float minDistance = Float.MAX_VALUE;
		for(int i=0; i<plans.size(); i++) {
			if(averageDistances[i] < minDistance) {
				minDistance = averageDistances[i];
				medoid = plans.get(i);
			}
		}
		return medoid;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof RelaxedPlan))
			return false;
		RelaxedPlan otherPlan = (RelaxedPlan)other;
		for(int i=0; i<actions.size(); i++) {
			if(!actions.get(i).event.equals(otherPlan.actions.get(i).event))
				return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		String str = "";
		for(PlanGraphActionNode action : actions)
			str += Text.BLANK + action + "\n";
		return str;
	}
}
