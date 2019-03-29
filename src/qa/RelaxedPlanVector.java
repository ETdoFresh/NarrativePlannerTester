package qa;

import sabre.Action;
import sabre.util.ImmutableArray;

public class RelaxedPlanVector {
	public ImmutableArray<Action> allActions;
	public float[] actionValues;
		
	public RelaxedPlanVector(ImmutableArray<Action> allActions, RelaxedPlan plan) {
		this.allActions = allActions;
		actionValues = new float[allActions.size()];
		
		for(int i = 0; i < allActions.size(); i++)
			if (plan.contains(allActions.get(i)))
				actionValues[i] = 1;
			else
				actionValues[i] = 0;
	}
	
	private RelaxedPlanVector(ImmutableArray<Action> allActions) {
		this.allActions = allActions;
		actionValues = new float[allActions.size()];
	}
	
	public int intersection(RelaxedPlanVector other) {
		int count = 0;
		for(int i=0; i<actionValues.length; i++) {
			if(actionValues[i] == 1 && other.actionValues[i] == 1)
				count++;
		}
		return count;
	}
	
	public int union(RelaxedPlanVector other) {
		int count = 0;
		for(int i=0; i<actionValues.length; i++) {
			if(actionValues[i]==1 || other.actionValues[i] ==1)
				count++;
		}
		return count;
	}
	
	public float magnitude()
	{
		float squaredSum = 0;
		for(float value : actionValues)
			squaredSum += Math.pow(value, 2);
		return (float) Math.sqrt(squaredSum);
	}
	
	@Override
	public String toString()
	{
		String str = "[";
		for(int i = 0; i < actionValues.length; i++)
			if (i == 0) str += (int)actionValues[i];
			else str += "," + (int)actionValues[i];
		str += "]";
		return str;
	}
	
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
}
