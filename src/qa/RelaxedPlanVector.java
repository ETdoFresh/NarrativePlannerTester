package qa;

import sabre.Action;
import sabre.util.ImmutableArray;

public class RelaxedPlanVector {
	public ImmutableArray<Action> allActions;
	public float[] values;
		
	public RelaxedPlanVector(ImmutableArray<Action> allActions, RelaxedPlan plan) {
		this.allActions = allActions;
		values = new float[allActions.size()];
		
		for(int i = 0; i < allActions.size(); i++)
			if (plan.contains(allActions.get(i)))
				values[i] = 1;
	}
	
	private RelaxedPlanVector(ImmutableArray<Action> allActions) {
		this.allActions = allActions;
		values = new float[allActions.size()];
	}
	
	public float magnitude()
	{
		float squaredSum = 0;
		for(float value : values)
			squaredSum += Math.pow(value, 2);
		return (float) Math.sqrt(squaredSum);
	}
	
	@Override
	public String toString()
	{
		String str = "[";
		for(int i = 0; i < values.length; i++)
			if (i == 0) str += (int)values[i];
			else str += "," + (int)values[i];
		str += "]";
		return str;
	}
	
	public RelaxedPlanVector plus(RelaxedPlanVector other) {		
		RelaxedPlanVector newVector = new RelaxedPlanVector(allActions);
//		if (values.length != other.values.length)
//			throw new Exception("Relaxed PLan Vectors are not of equal length");
		
		for (int i = 0; i < values.length; i++)
			newVector.values[i] = values[i] + other.values[i];
		
		return newVector;
	}

	public RelaxedPlanVector minus(RelaxedPlanVector other) {		
		RelaxedPlanVector newVector = new RelaxedPlanVector(allActions);
//		if (values.length != other.values.length)
//			throw new Exception("Relaxed PLan Vectors are not of equal length");
		
		for (int i = 0; i < values.length; i++)
			newVector.values[i] = values[i] - other.values[i];
		
		return newVector;
	}
}
