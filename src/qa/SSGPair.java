package qa;

import sabre.Event;
import sabre.logic.Literal;

public class SSGPair {
	public Event satisfyingStep;
	public Literal goal;
	
	public SSGPair(Event satisfyingStep, Literal goal) {
		this.satisfyingStep = satisfyingStep;
		this.goal = goal;
	}
	
	@Override
	public String toString() {
		return "[" + satisfyingStep + ", " + goal + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((goal == null) ? 0 : goal.hashCode());
		result = prime * result + ((satisfyingStep == null) ? 0 : satisfyingStep.hashCode());
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
		SSGPair other = (SSGPair) obj;
		if (goal == null) {
			if (other.goal != null)
				return false;
		} else if (!CheckEquals.Literal(goal, other.goal))
			return false;
		if (satisfyingStep == null) {
			if (other.satisfyingStep != null)
				return false;
		} else if (!satisfyingStep.equals(other.satisfyingStep))
			return false;
		return true;
	}	
}
