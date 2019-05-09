package qa;

import java.util.HashSet;

import sabre.Agent;
import sabre.Domain;
import sabre.Event;
import sabre.graph.PlanGraphEventNode;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Literal;

// (Agent, Goal, Satisfy Step Schema) Triple
public class AGSSSTriple {
	public Agent agent;
	public Literal goal;
	public String satisfyingStepSchema;

	public AGSSSTriple(Agent agent, Literal goal, String satisfyingStepSchema) {
		this.agent = agent;
		this.goal = goal;
		this.satisfyingStepSchema = satisfyingStepSchema;
	}

	public static HashSet<AGSSSTriple> GetByStep(RelaxedNode node) {
		HashSet<AGSSSTriple> pairs = new HashSet<>();
		Domain domain = node.eventNode.graph.space.domain;
		for (Agent agent : domain.agents)
			for (ConjunctiveClause goal : AgentGoal.get(domain, agent).toDNF().arguments)
				for (Literal goalLiteral : goal.arguments)
					if (node.goalLiteralSatisfiedByStep.contains(goalLiteral))
						pairs.add(new AGSSSTriple(agent, goalLiteral, node.schema));
		return pairs;
	}

	public static HashSet<AGSSSTriple> GetByPlan(RelaxedPlan plan) {
		HashSet<AGSSSTriple> pairs = new HashSet<>();
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
		AGSSSTriple other = (AGSSSTriple) obj;
		if (agent == null) {
			if (other.agent != null)
				return false;
		} else if (!agent.equals(other.agent))
			return false;
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
		return "[" + agent + ", " + goal + ", " + satisfyingStepSchema + "]";
	}
}
