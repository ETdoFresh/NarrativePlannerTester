package qa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import sabre.space.SearchSpace;

public class Comparisons {
	private SearchSpace space;
	private ArrayList<RelaxedPlan> plans;
	public ArrayList<Comparison> comparisons = new ArrayList<>();

	private Comparisons(SearchSpace space, ArrayList<RelaxedPlan> plans) {
		this.space = space;
		this.plans = plans;
	}

	public static Comparisons compute(SearchSpace space, ArrayList<RelaxedPlan> plans) {
		Comparisons comparisons = new Comparisons(space, plans);
		comparisons.compute();
		return comparisons;
	}

	private void compute() {
		
//		float[] max = Vector.getAgentSchemaVector(space, plans.get(0));
//		// Prevent Divide by 0
//		for (int i = 0; i < max.length; i++)
//			max[i] = 1;
//		// Get Max!
//		for (int i = 0; i < plans.size(); i++) {
//			float[] vector = Vector.getAgentSchemaVector(space, plans.get(i));
//			for (int j = 0; j < vector.length; j++)
//				if (max[j] < vector[j])
//					max[j] = vector[j];
//		}
//		
//		for (int i = 0; i < plans.size(); i++)
//			for (int j = i + 1; j < plans.size(); j++) {
//				RelaxedPlan a = plans.get(i);
//				RelaxedPlan b = plans.get(j);
//				String name;
//				float[] vectorA, vectorB;
//				ArrayList<DistanceMeasure> measures = new ArrayList<>();
//
//				name = "Action Distance"; //-----------------------------------
//				vectorA = Vector.getSchemaVector(space, a);
//				vectorB = Vector.getSchemaVector(space, b);
//				measures.add(
//						new DistanceMeasure(name, vectorA, vectorB, Vector.distance(vectorA, vectorB)));
//
//				name = "Action Distance Normalized"; //-----------------------------------
//				vectorA = Vector.normalize(vectorA);
//				vectorB = Vector.normalize(vectorB);
//				measures.add(
//						new DistanceMeasure(name, vectorA, vectorB, Vector.distance(vectorA, vectorB)));
//				
//				name = "Agent + Schema Distance"; //-----------------------------------
//				vectorA = Vector.getAgentSchemaVector(space, a);
//				vectorB = Vector.getAgentSchemaVector(space, b);
//				measures.add(
//						new DistanceMeasure(name, vectorA, vectorB, Vector.distance(vectorA, vectorB)));
//
//				name = "Agent + Schema Distance Normalized"; //-----------------------------------
//				vectorA = Vector.normalize(vectorA);
//				vectorB = Vector.normalize(vectorB);
//				measures.add(
//						new DistanceMeasure(name, vectorA, vectorB, Vector.distance(vectorA, vectorB)));
//				
//				name = "Agent + Schema Distance Divide by Max Occurences"; //-----------------------------------
//				vectorA = Vector.getAgentSchemaVector(space, a);
//				vectorB = Vector.getAgentSchemaVector(space, b);
//				vectorA = Vector.divideComponentWise(vectorA, max);
//				vectorB = Vector.divideComponentWise(vectorB, max);
//				measures.add(
//						new DistanceMeasure(name, vectorA, vectorB, Vector.distance(vectorA, vectorB)));
//				
//				name = "Goal distance"; //-----------------------------------
//				vectorA = Vector.getGoalVector(space, a);
//				vectorB = Vector.getGoalVector(space, b);
//				measures.add(
//						new DistanceMeasure(name, vectorA, vectorB, Vector.distance(vectorA, vectorB)));
//				
//				comparisons.add(new Comparison(a, b, measures));
//			}
	}

	@Override
	public String toString() {
		String json = "[\n";
		for (int i = 0; i < comparisons.size(); i++)
			json += i == 0 ? comparisons.get(i) : "," + comparisons.get(i);
		json += "]";
		return json;
	}

	public class DistanceMeasure {
		public String name;
		public float[] vectorA;
		public float[] vectorB;
		public float[] vectorMax;
		public float distance;

		public DistanceMeasure(String name, float[] vectorA, float[] vectorB, float[] vectorMax, float distance) {
			this.name = name;
			this.vectorA = vectorA;
			this.vectorB = vectorB;
			this.vectorMax = vectorMax;
			this.distance = distance;
		}

		public DistanceMeasure(String name, float[] vectorA, float[] vectorB, float distance) {
			this.name = name;
			this.vectorA = vectorA;
			this.vectorB = vectorB;
			this.distance = distance;
		}

		@Override
		public String toString() {
			String output = name + "\\n";
			output += "VectorA: [";
			for (int i = 0; i < vectorA.length; i++)
				output += i == 0 ? vectorA[i] : "," + vectorA[i];
			output += "]\\nVectorB: [";
			for (int i = 0; i < vectorB.length; i++)
				output += i == 0 ? vectorB[i] : "," + vectorB[i];
			output += "]\\nDistance: " + distance  + "\\n";
			return output;
		}
	}

	public class Comparison {
		public RelaxedPlan left;
		public RelaxedPlan right;
		public ArrayList<DistanceMeasure> measures = new ArrayList<>();

		public Comparison(RelaxedPlan left, RelaxedPlan right, ArrayList<DistanceMeasure> measures) {
			this.left = left;
			this.right = right;
			this.measures = measures;
		}

		@Override
		public String toString() {
			String json = "{\n";
			json += "\"LeftStory\":\"";
			for (int i = 0; i < left.size(); i++)
				json += i == 0 ? left.get(i) : "\\n" + left.get(i);
			json += "\",\n\"RightStory\":\"";
			for (int i = 0; i < right.size(); i++)
				json += i == 0 ? right.get(i) : "\\n" + right.get(i);
			json += "\",\n\"Stats\":\"";
			for (int i = 0; i < measures.size(); i++)
				json += measures.get(i) + "\\n";
			json += "\"\n}\n";
			return json;
		}
	}

	public void keepRandomSet(int size) {
		Random random = new Random();
		while (comparisons.size() > size)
			comparisons.remove(random.nextInt(comparisons.size()));
	}
}
