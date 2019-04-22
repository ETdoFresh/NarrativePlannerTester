package qa;

import java.io.Serializable;
import java.util.Stack;

import sabre.Agent;
import sabre.Event;
import sabre.graph.PlanGraphActionNode;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Expression;
import sabre.logic.Literal;

public class Explanation implements Serializable {
	public Agent agent;
	public Expression goals;
	public CausalChainSet causalChainSet;
	public Stack<Event> steps;

	public Explanation(Agent agent, Expression agentGoal) {
		this.agent = agent;
		this.goals = agentGoal;
		this.steps = new Stack<>();
		for (ConjunctiveClause goal : goals.toDNF().arguments) {
			causalChainSet = new CausalChainSet(goal); // TODO remove this later and fix code to handle DNF
		}
	}

	private Explanation(Agent agent, Expression goals, CausalChainSet causalChainSet, Stack<Event> steps) {
		this.agent = agent;
		this.goals = goals;
		this.causalChainSet = causalChainSet.clone();
		this.steps = steps;
	}

	public Explanation clone() {
		return new Explanation(agent, goals, causalChainSet, steps);
	}

	public void applyEvent(Event event) {
		causalChainSet.extendOrRemoveChainUsing(event);
		steps.push(event);
	}

	public boolean containsEffect(Event event) {
		for (ConjunctiveClause disjunct : event.effect.toDNF().arguments)
			for (Literal literal : disjunct.arguments)
				for (Literal head : causalChainSet.heads())
					if (CheckEquals.Literal(literal, head))
						return true;

		return false;
	}

	public boolean canExtendAtLeastOneChain(PlanGraphActionNode actionNode) {
		for (ConjunctiveClause disjunct : actionNode.event.effect.toDNF().arguments)
			for (Literal effectLiteral : disjunct.arguments)
				for (CausalChain causalChain : causalChainSet.causalChains)
					if (CheckEquals.Literal(causalChain.head(), effectLiteral))
						for (ConjunctiveClause p : actionNode.event.precondition.toDNF().arguments)
							for (Literal preconditionLiteral : p.arguments)
								if (causalChain.canExtend(preconditionLiteral))
									return true;

		return false;
	}

	@Override
	public String toString() {
		return "(" + agent + " Explanation: " + causalChainSet + ")";
	}

	public void noveltyPruneChains() {
		for (int i = causalChainSet.causalChains.size() - 1; i >= 0; i--) {
			CausalChain chain = causalChainSet.causalChains.get(i);
			for (int j = causalChainSet.causalChains.size() - 1; j >= 0; j--) {
				CausalChain otherChain = causalChainSet.causalChains.get(j);

				if (!chain.equals(otherChain))
					if (chain.head().equals(otherChain.head()))
						if (chain.tail().equals(otherChain.tail()))
							if (otherChain.history.containsAll(chain.history)) {
								causalChainSet.causalChains.remove(i);
								break;
							}
			}
		}
	}

	public Explanation add(PlanGraphActionNode actionNode) {
		if (!containsEffect(actionNode.event))
			return null;
		
		Explanation newExplanation = clone();
		newExplanation.applyEvent(actionNode.event);
		return newExplanation;
	}
}
