package qa;

import java.util.ArrayList;
import java.util.HashSet;

public class Vector {

	public static float[] getEmpty(Distance distance) {
		switch (distance.distanceMetric) {
		case SATSTEP_GOAL:
			return new float[DomainSet.getAllSSGPairs().size()];
		case SATSTEP_SCHEMA_GOAL:
			return new float[DomainSet.getAllSSSGPairs().size()];
		case AUTHOR_SATSTEP_SCHEMA_GOAL:
			return new float[DomainSet.getAllASSSGPairs().size()];
		case SATSTEP_GOAL_PAIR_SCHEMAS_WEIGTHED:
			return new float[DomainSet.getAllASSSGPairs().size() + DomainSet.getAllSSGPairs().size()
					+ DomainSet.getAllSSSGPairs().size()];
		default:
			return null;
		}
	}

	public static float[] get(RelaxedPlan plan, Distance distance) {
		switch (distance.distanceMetric) {
		case SATSTEP_GOAL:
			return getSSGVector(plan);
		case SATSTEP_SCHEMA_GOAL:
			return getSSSGVector(plan);
		case AUTHOR_SATSTEP_SCHEMA_GOAL:
			return getASSSGVector(plan);
		case SATSTEP_GOAL_PAIR_SCHEMAS_WEIGTHED:
			return getWeighted(plan, distance);
		default:
			return null;
		}
	}

	public static float getWeightedDistance(float[] a, float[] b) {
		int length1 = DomainSet.getAllASSSGPairs().size();
		int length2 = DomainSet.getAllSSSGPairs().size();
		int length3 = DomainSet.getAllSSGPairs().size();
		float[] aASSSG = get(a, 0, length1);
		float[] aSSSG = get(a, length1, length2);
		float[] aSSG = get(a, length2, length3);
		float[] bASSSG = get(b, 0, length1);
		float[] bSSSG = get(b, length1, length2);
		float[] bSSG = get(b, length2, length3);
		return 0.7f * distance(aASSSG, bASSSG) + 0.2f * distance(aSSSG, bSSSG) + 0.1f * distance(aSSG, bSSG);
	}

	private static float[] get(float[] a, int start, int length) {
		float[] vector = new float[length];
		for (int i = 0; i < length; i++)
			vector[i] = a[start + i];
		return vector;
	}

	private static float[] getWeighted(RelaxedPlan plan, Distance distance) {
		float[] vector = getEmpty(distance);
		float[] vector1 = getASSSGVector(plan);
		float[] vector2 = getSSSGVector(plan);
		float[] vector3 = getSSGVector(plan);
		for (int i = 0; i < vector1.length; i++)
			vector[i] = vector1[i];
		for (int i = 0; i < vector2.length; i++)
			vector[i + vector1.length] = vector2[i];
		for (int i = 0; i < vector3.length; i++)
			vector[i + vector1.length + vector2.length] = vector3[i];
		return vector;
	}

	private static float[] getASSSGVector(RelaxedPlan plan) {
		ArrayList<ASSSGPair> all = new ArrayList<>(DomainSet.getAllASSSGPairs());
		float[] vector = new float[all.size()];
		HashSet<ASSSGPair> pairs = ASSSGPair.GetByPlan(plan);
		for (int i = 0; i < all.size(); i++)
			if (pairs.contains(all.get(i)))
				vector[i] = 1;
		return vector;
	}

	private static float[] getSSSGVector(RelaxedPlan plan) {
		ArrayList<SSSGPair> all = new ArrayList<>(DomainSet.getAllSSSGPairs());
		float[] vector = new float[all.size()];
		HashSet<SSSGPair> pairs = SSSGPair.GetByPlan(plan);
		for (int i = 0; i < all.size(); i++)
			if (pairs.contains(all.get(i)))
				vector[i] = 1;
		return vector;
	}

	private static float[] getSSGVector(RelaxedPlan plan) {
		ArrayList<SSGPair> all = new ArrayList<>(DomainSet.getAllSSGPairs());
		float[] vector = new float[all.size()];
		for (int i = 0; i < all.size(); i++)
			if (plan.getSSGPairs().contains(all.get(i)))
				vector[i] = 1;
		return vector;
	}

	public static float[] add(float[] lhs, float[] rhs) {
		float[] sum = lhs.clone();
		for (int i = 0; i < sum.length; i++)
			sum[i] += rhs[i];
		return sum;
	}

	public static float squareMagnitude(float[] vector) {
		float squareMagnitude = 0;
		for (float value : vector)
			squareMagnitude += value * value;
		return squareMagnitude;
	}

	public static float magnitude(float[] vector) {
		return (float) Math.sqrt(squareMagnitude(vector));
	}

	public static float squareDistance(float[] vectorA, float[] vectorB) {
		float[] difference = vectorA.clone();
		for (int i = 0; i < difference.length; i++)
			difference[i] -= vectorB[i];
		return squareMagnitude(difference);
	}

	public static float distance(float[] vectorA, float[] vectorB) {
		return (float) Math.sqrt(squareDistance(vectorA, vectorB));
	}

	public static float[] divide(float[] vector, float divisor) {
		float[] divided = vector.clone();
		for (int i = 0; i < divided.length; i++)
			divided[i] /= divisor;
		return divided;
	}

	public static float[] divideComponentWise(float[] vector, float[] divisor) {
		float[] divided = vector.clone();
		for (int i = 0; i < divided.length; i++)
			divided[i] /= divisor[i];
		return divided;
	}

	public static float[] normalize(float[] vector) {
		return divide(vector, magnitude(vector));
	}
}
