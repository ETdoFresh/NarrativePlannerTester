package qa;

import java.util.HashSet;

import sabre.Event;

public class SLPair {
	public Event step;
	public int level;
	
	public SLPair(Event step, int level) {
		this.step = step;
		this.level = level;
	}
	
	public static HashSet<SLPair> GetByStep(RelaxedNode node) {
		HashSet<SLPair> pairs = new HashSet<>();
		pairs.add(new SLPair(node.eventNode.event, node.level));
		return pairs;
	}

	public static HashSet<SLPair> GetByPlan(RelaxedPlan plan) {
		HashSet<SLPair> pairs = new HashSet<>();
		for (RelaxedNode node : plan)
			pairs.addAll(GetByStep(node));
		return pairs;
	}
	
	@Override
	public String toString() {
		return "[" + step + ", " + level + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + level;
		result = prime * result + ((step == null) ? 0 : step.hashCode());
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
		SLPair other = (SLPair) obj;
		if (level != other.level)
			return false;
		if (step == null) {
			if (other.step != null)
				return false;
		} else if (!step.equals(other.step))
			return false;
		return true;
	}
}
