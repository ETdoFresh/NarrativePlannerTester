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
	public boolean equals(Object obj) {
		if (!(obj instanceof SSGPair))
			return false;
		
		SSGPair other = (SSGPair)obj;
		return satisfyingStep.equals(other.satisfyingStep)
				&& CheckEquals.Literal(goal, other.goal);
	}
	
	@Override
	public String toString() {
		return "[" + satisfyingStep + ", " + goal + "]";
	}
}
