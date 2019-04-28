package qa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import sabre.Agent;
import sabre.Domain;
import sabre.Event;
import sabre.graph.PlanGraphEventNode;
import sabre.graph.PlanGraphLiteralNode;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Literal;

public class RelaxedNode implements Serializable {
	private static final long serialVersionUID = 1L;

	public PlanGraphEventNode eventNode;
	public ArrayList<Explanation> explanations = new ArrayList<>();
	public HashSet<Literal> inServiceOfGoalLiteral = new HashSet<>();
	public HashSet<Agent> inServiceOfAgent = new HashSet<>();
	public int level;

	public RelaxedNode(PlanGraphEventNode eventNode, ArrayList<Explanation> explanations, int level) {
		super();
		this.eventNode = eventNode;
		this.explanations = explanations;
		this.level = level;

		if (explanations != null && explanations.size() > 0)
			GetWhatThisStepIsServing();
	}

	private void GetWhatThisStepIsServing() {
		for (Explanation explanation : explanations)
			for (Event event : explanation.steps)
				if (event.equals(eventNode.event))
					inServiceOfAgent.add(explanation.agent);

		Domain domain = eventNode.graph.space.domain;
		for (Literal goalLiteral : AgentGoal.getCombinedAuthorAndAllAgentGoals(domain))
			for (Explanation explanation : explanations) {
				if (inServiceOfGoalLiteral.contains(goalLiteral))
					break;
				for (CausalChain chain : explanation.causalChainSet.causalChains) {
					if (inServiceOfGoalLiteral.contains(goalLiteral))
						break;
					if (chain.history.size() > 1) {
						for (ConjunctiveClause precondition : eventNode.event.precondition.toDNF().arguments) {
							if (inServiceOfGoalLiteral.contains(goalLiteral))
								break;
							for (Literal literal : precondition.arguments) {
								if (literal.equals(chain.head()))
									if (goalLiteral.equals(chain.tail()))
										inServiceOfGoalLiteral.add(goalLiteral);
							}
						}
					}
				}
			}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof RelaxedNode) {
			RelaxedNode otherNode = (RelaxedNode) other;
			if (explanations == null && otherNode.explanations == null)
				return eventNode.equals(otherNode.eventNode) && level == otherNode.level;
			else if (explanations != null && otherNode.explanations != null)
				return eventNode.equals(otherNode.eventNode) && level == otherNode.level
						&& explanations.equals(otherNode.explanations);
		}
		return false;
	}

	@Override
	public String toString() {
		return "[" + eventNode.event + " level=" + level + "]";
	}
}
