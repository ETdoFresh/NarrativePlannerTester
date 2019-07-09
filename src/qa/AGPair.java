package qa;

import java.util.HashSet;

import sabre.Agent;
import sabre.Domain;
import sabre.Event;
import sabre.graph.PlanGraphEventNode;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Literal;

public class AGPair {
	public Agent agent;
	public Literal goal;

	public AGPair(Agent agent, Literal goal) {
		this.agent = agent;
		this.goal = goal;
	}

	public static HashSet<AGPair> GetByStep(RelaxedNode node) {
		HashSet<AGPair> pairs = new HashSet<>();
		Domain domain = node.eventNode.graph.space.domain;
		for (Agent agent : domain.agents)
			for (ConjunctiveClause goal : AgentGoal.get(domain, agent).toDNF().arguments)
				for (Literal goalLiteral : goal.arguments)
					if (node.inServiceOfGoalLiteral.contains(goalLiteral))
						pairs.add(new AGPair(agent, goalLiteral));
		return pairs;
	}

	public static HashSet<AGPair> GetByPlan(RelaxedPlan plan) {
		HashSet<AGPair> pairs = new HashSet<>();
		for (RelaxedNode node : plan)
			pairs.addAll(GetByStep(node));
		return pairs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agent == null) ? 0 : agent.hashCode());
		result = prime * result + ((goal == null) ? 0 : goal.hashCode());
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
		AGPair other = (AGPair) obj;
		if (agent == null) {
			if (other.agent != null)
				return false;
		} else if (!agent.equals(other.agent))
			return false;
		if (goal == null) {
			if (other.goal != null)
				return false;
		} else if (!CheckEquals.Literal(goal,  other.goal))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + agent + ", " + goal + "]";
	}
}
