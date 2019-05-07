package qa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import sabre.Action;
import sabre.Agent;
import sabre.Domain;
import sabre.Event;
import sabre.graph.PlanGraphEventNode;
import sabre.graph.PlanGraphLiteralNode;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Literal;
import sabre.logic.Logical;
import sabre.logic.Term;

public class RelaxedNode implements Serializable {
	private static final long serialVersionUID = 1L;

	public PlanGraphEventNode eventNode;
	public ArrayList<Explanation> explanations = new ArrayList<>();
	public String schema;
	public HashSet<Agent> consenting = new HashSet<>();
	public HashSet<Agent> allAgentsInvolved = new HashSet<>();
	public HashSet<Agent> inServiceOfAgentGoal = new HashSet<>();
	public HashSet<Literal> inServiceOfGoalLiteral = new HashSet<>();
	public HashSet<Agent> agentsGoalSatisfiedByStep = new HashSet<>();
	public HashSet<Literal> goalLiteralSatisfiedByStep = new HashSet<>();
	public HashSet<SSGPair> satisfyingStepGoalLiteralPairs = new HashSet<>();
	public int level;

	public RelaxedNode(PlanGraphEventNode eventNode, ArrayList<Explanation> explanations, int level) {
		super();
		this.eventNode = eventNode;
		this.explanations = explanations;
		this.level = level;

		if (explanations != null && explanations.size() > 0) {
			PopulateSchema();
			PopulateConsensting();
			PopulateAllAgentsInvolved();
			PopulateInServiceOfGoalLiteral();
			PopulateInServiceOfAgentLiteral();
			PopulateAgentGoalStatisfiedByStep();
			PopulateGoalLiteralStatisfiedByStep();
			ComputeAuthorGoalLiteralDistance();
		}
		PopulateSSGPair();
	}

	private void PopulateSchema() {
		schema = eventNode.event.name;
	}

	private void PopulateConsensting() {
		if (eventNode.event instanceof Action)
			for (Term term : ((Action) eventNode.event).agents)
				if (term instanceof Agent)
					consenting.add((Agent) term);
	}

	private void PopulateAllAgentsInvolved() {
		if (eventNode.event instanceof Action)
			for (Logical parameter : ((Action) eventNode.event).arguments)
				if (parameter instanceof Agent)
					allAgentsInvolved.add((Agent) parameter);
	}

	private void PopulateInServiceOfGoalLiteral() {
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

	private void PopulateInServiceOfAgentLiteral() {
		Domain domain = eventNode.graph.space.domain;
		for (Agent agent : domain.agents)
			for (ConjunctiveClause goal : AgentGoal.get(domain, agent).toDNF().arguments) {
				if (inServiceOfAgentGoal.contains(agent))
					break;
				for (Literal goalLiteral : goal.arguments)
					if (inServiceOfGoalLiteral.contains(goalLiteral)) {
						inServiceOfAgentGoal.add(agent);
						break;
					}
			}
	}

	private void PopulateAgentGoalStatisfiedByStep() {
		Domain domain = eventNode.graph.space.domain;
		for (Agent agent : domain.agents)
			for (ConjunctiveClause goal : AgentGoal.get(domain, agent).toDNF().arguments) {
				if (agentsGoalSatisfiedByStep.contains(agent))
					break;
				for (Literal goalLiteral : goal.arguments) {
					if (agentsGoalSatisfiedByStep.contains(agent))
						break;
					for (ConjunctiveClause effect : eventNode.event.effect.toDNF().arguments) {
						if (agentsGoalSatisfiedByStep.contains(agent))
							break;
						for (Literal effectLiteral : effect.arguments)
							if (CheckEquals.Literal(goalLiteral, effectLiteral)) {
								agentsGoalSatisfiedByStep.add(agent);
								break;
							}
					}
				}
			}
	}

	private void PopulateGoalLiteralStatisfiedByStep() {
		for (Literal goalLiteral : inServiceOfGoalLiteral) {
			for (ConjunctiveClause effect : eventNode.event.effect.toDNF().arguments) {
				for (Literal effectLiteral : effect.arguments)
					if (CheckEquals.Literal(goalLiteral, effectLiteral)) {
						goalLiteralSatisfiedByStep.add(goalLiteral);
						break;
					}
			}
		}
	}

	private void ComputeAuthorGoalLiteralDistance() {
		// TODO Auto-generated method stub

	}

	private void PopulateSSGPair() {
		Domain domain = eventNode.graph.space.domain;
		for (Literal goalLiteral : AgentGoal.getCombinedAuthorAndAllAgentGoals(domain))
			for (ConjunctiveClause effect : eventNode.event.effect.toDNF().arguments)
				for (Literal effectLiteral : effect.arguments)
					if (CheckEquals.Literal(goalLiteral, effectLiteral))
						satisfyingStepGoalLiteralPairs.add(new SSGPair(eventNode.event, goalLiteral));
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
