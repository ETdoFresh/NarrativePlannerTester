package qa;

import java.util.ArrayList;
import java.util.Random;
import sabre.Action;
import sabre.space.SearchSpace;

public class RelaxedPlanVector {

	public boolean[] actionValues;
	public int size;
	public int clusterAssignment = -1;

	protected static SearchSpace space;
	
	/** Construct a vector from a plan */
	public RelaxedPlanVector(SearchSpace space, RelaxedPlan plan) {
		RelaxedPlanVector.space = space;
		this.size = space.actions.size();
		actionValues = new boolean[size];		
		for(int i = 0; i < size; i++)
			if (plan.contains(space.actions.get(i)))
				actionValues[i] = true;
			else
				actionValues[i] = false;
	}
	
	/** Construct a randomized vector (use for initial cluster centroids) */
	public RelaxedPlanVector(SearchSpace space, float weight) {
		RelaxedPlanVector.space = space;
		this.size = space.actions.size();
		this.actionValues = new boolean[size];
		Random random = new Random();
		for(int i=0; i<size; i++) {
			if(random.nextFloat() < weight)
				actionValues[i] = true;
			else
				actionValues[i] = false;
		}
	}
	
	/** Get the list of actions represented by this vector */
	public ArrayList<Action> toActionList() {
		ArrayList<Action> actions = new ArrayList<>();
		for(int i=0; i<size; i++) {
			if(actionValues[i])
				actions.add(space.actions.get(i));
		}
		return actions;
	}
	
	/** Get the Jaccard distance between this and another vector */
	public float jaccard(RelaxedPlanVector other) {
		return 1 - (float)this.intersection(other) / (float)this.union(other);
	}
	
	/** Get the intersection of this and another vector */
	protected int intersection(RelaxedPlanVector other) {
		if(size != other.size) {
			System.out.println("Huh? Vectors not the same size: "+size+", "+other.size);
			return -1;
		}
		int count = 0;
		for(int i=0; i<actionValues.length; i++) {
			if(actionValues[i] == true && other.actionValues[i] == true)
				count++;
		}
		return count;
	}
	
	/** Get the union of this and another vector */
	protected int union(RelaxedPlanVector other) {
		int count = 0;
		for(int i=0; i<actionValues.length; i++) {
			if(actionValues[i] == true || other.actionValues[i] == true)
				count++;
		}
		return count;
	}	
	
	/** Get a vector representing the mean of a list of vectors */
	public static RelaxedPlanVector mean(ArrayList<RelaxedPlanVector> planVecs) {
		RelaxedPlanVector meanVector = new RelaxedPlanVector(space, 0);
		// for each slot in the new vector
		for(int i=0; i<meanVector.size; i++) {
			// get the mean of the values in that slot from the vectors in the list 
			float sum = 0;
			for(int j=0; j<planVecs.size(); j++) {
				if(planVecs.get(j).actionValues[i])
					sum++;
			}
			// set its value to that mean
			if((sum / planVecs.size()) >= 0.5)
				meanVector.actionValues[i] = true;
			else
				meanVector.actionValues[i] = false;			
		}		
		return meanVector;
	}

	public static RelaxedPlanVector medoid(ArrayList<RelaxedPlanVector> planVecs) {
		RelaxedPlanVector medoid = null;
		float[] averageDistances = new float[planVecs.size()];
		for(int i=0; i<planVecs.size(); i++) {
			float sum = 0;
			for(RelaxedPlanVector other : planVecs) {
				sum += planVecs.get(i).jaccard(other);
			}
			averageDistances[i] = sum / planVecs.size();
		}
		float minDistance = Float.MAX_VALUE;
		for(int i=0; i<planVecs.size(); i++) {
			if(averageDistances[i] < minDistance) {
				minDistance = averageDistances[i];
				medoid = planVecs.get(i);
			}
		}
		if(medoid==null)
			return new RelaxedPlanVector(space, 0);
		return medoid;
	}
	
	/** Count up the 1's in this vector */
	public float sum() {
		float sum = 0;
		for(int i=0; i<size; i++) {
			if(actionValues[i])
				sum++;
		}
		return sum;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof RelaxedPlanVector))
			return false;
		RelaxedPlanVector otherVector = (RelaxedPlanVector)other;
		for(int i=0; i<actionValues.length; i++) {
			if(actionValues[i] != otherVector.actionValues[i])
				return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		String str = "[";
		for(int i = 0; i < actionValues.length; i++) {
			if (i == 0) {
				if(actionValues[i]) str += "T";
				else str += "F";
			}
			else if(actionValues[i]) str += ",T";
			else str += ",F";
		}
		str += "]";
		str += "\nActions:";
		for(Action action : toActionList())
			str += "\n\t" + action;
		return str;
	}

	/*
	public RelaxedPlanVector plus(RelaxedPlanVector other) {		
		RelaxedPlanVector newVector = new RelaxedPlanVector(allActions);
//		if (values.length != other.values.length)
//			throw new Exception("Relaxed PLan Vectors are not of equal length");
		
		for (int i = 0; i < actionValues.length; i++)
			newVector.actionValues[i] = actionValues[i] + other.actionValues[i];
		
		return newVector;
	}

	public RelaxedPlanVector minus(RelaxedPlanVector other) {		
		RelaxedPlanVector newVector = new RelaxedPlanVector(allActions);
//		if (values.length != other.values.length)
//			throw new Exception("Relaxed PLan Vectors are not of equal length");
		
		for (int i = 0; i < actionValues.length; i++)
			newVector.actionValues[i] = actionValues[i] - other.actionValues[i];
		
		return newVector;
	}
	*/
}
