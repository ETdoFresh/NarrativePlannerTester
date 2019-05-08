package qa;

import java.util.HashSet;

import sabre.logic.Literal;

public class SSSGPair {
	public Literal goal;
	public String satisfyingStepSchema;

	public SSSGPair(Literal goal, String satisfyingStepSchema) {
		this.goal = goal;
		this.satisfyingStepSchema = satisfyingStepSchema;
	}

	public static HashSet<SSSGPair> GetByStep(RelaxedNode node) {
		HashSet<SSSGPair> pairs = new HashSet<>();
		for (SSGPair ssgPair : node.satisfyingStepGoalLiteralPairs)
			pairs.add(new SSSGPair(ssgPair.goal, ssgPair.satisfyingStep.name));
		return pairs;
	}

	public static HashSet<SSSGPair> GetByPlan(RelaxedPlan plan) {
		HashSet<SSSGPair> pairs = new HashSet<>();
		for (RelaxedNode node : plan)
			pairs.addAll(GetByStep(node));
		return pairs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((goal == null) ? 0 : goal.hashCode());
		result = prime * result + ((satisfyingStepSchema == null) ? 0 : satisfyingStepSchema.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SSSGPair other = (SSSGPair) obj;
		if (goal == null) {
			if (other.goal != null)
				return false;
		} else if (!CheckEquals.Literal(goal, other.goal))
			return false;
		if (satisfyingStepSchema == null) {
			if (other.satisfyingStepSchema != null)
				return false;
		} else if (!satisfyingStepSchema.equals(other.satisfyingStepSchema))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + satisfyingStepSchema + ", " + goal + "]";
	}
}
