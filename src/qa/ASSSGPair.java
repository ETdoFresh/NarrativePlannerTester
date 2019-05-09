package qa;

import java.util.HashSet;

import sabre.logic.ConjunctiveClause;
import sabre.logic.Literal;

// (Author Satisfy Step Schema, Goal) Pair
public class ASSSGPair {
	public Literal goal;
	public String satisfyingStepSchema;

	public ASSSGPair(Literal goal, String satisfyingStepSchema) {
		this.goal = goal;
		this.satisfyingStepSchema = satisfyingStepSchema;
	}
	
	public static HashSet<ASSSGPair> GetByStep(RelaxedNode node) {
		HashSet<ASSSGPair> pairs = new HashSet<>();
		for (ConjunctiveClause goals : node.eventNode.graph.space.domain.goal.toDNF().arguments)
			for (Literal goal : goals.arguments)
				for (SSGPair ssgPair : node.satisfyingStepGoalLiteralPairs)
					if (CheckEquals.Literal(ssgPair.goal, goal))
						pairs.add(new ASSSGPair(goal, ssgPair.satisfyingStep.name));
		return pairs;
	}

	public static HashSet<ASSSGPair> GetByPlan(RelaxedPlan plan) {
		HashSet<ASSSGPair> pairs = new HashSet<>();
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
		ASSSGPair other = (ASSSGPair) obj;
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
