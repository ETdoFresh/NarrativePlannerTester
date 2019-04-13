package qa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import sabre.Agent;
import sabre.Event;
import sabre.graph.PlanGraphActionNode;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Expression;
import sabre.logic.Literal;
import sabre.util.ImmutableArray;

public class Explanation {
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

	public boolean canExtendAtLeastOneCluster(PlanGraphActionNode actionNode) {
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

	public class CausalChainSet {
		ArrayList<CausalChain> causalChains = new ArrayList<>();

		public CausalChainSet(ConjunctiveClause goal) {
			for (Literal goalLiteral : goal.arguments)
				causalChains.add(new CausalChain(goalLiteral));
		}

		private CausalChainSet(Iterable<CausalChain> causalChains) {
			for (CausalChain causalChain : causalChains)
				this.causalChains.add(causalChain.clone());
		}

		public CausalChainSet clone() {
			return new CausalChainSet(causalChains);
		}

		public void extendOrRemoveChainUsing(Event currentStep) {
			for (ConjunctiveClause e : currentStep.effect.toDNF().arguments)
				for (Literal effectLiteral : e.arguments)
					for (int i = causalChains.size() - 1; i >= 0; i--) {
						CausalChain causalChain = causalChains.get(i);
						if (CheckEquals.Literal(causalChain.head(), effectLiteral)) {
							for (ConjunctiveClause p : currentStep.precondition.toDNF().arguments)
								for (Literal preconditionLiteral : p.arguments)
									if (causalChain.canExtend(preconditionLiteral))
										causalChains.add(causalChain.push(preconditionLiteral));
							causalChains.remove(i);
						}
					}
		}

		public boolean headContains(Literal literal) {
			for (CausalChain causalChain : causalChains)
				if (causalChain.head().equals(literal))
					return true;

			return false;
		}

		public Iterable<Literal> heads() {
			ArrayList<Literal> heads = new ArrayList<>();
			for (CausalChain causalChain : causalChains)
				heads.add(causalChain.head());
			return heads;
		}

		@Override
		public String toString() {
			String output = "";
			for (CausalChain chain : causalChains) {
				output += chain.equals(causalChains.get(0)) ? "" : ", ";
				output += chain.head();
			}
			return output;
		}
	}

	public class CausalChain {
		public ArrayList<Literal> history = new ArrayList<>();

		public CausalChain(Literal goalLiteral) {
			history.add(goalLiteral);
		}

		private CausalChain(ArrayList<Literal> history) {
			this.history.addAll(history);
		}

		public CausalChain clone() {
			return new CausalChain(history);
		}

		public boolean canExtend(Literal literal) {
			for (Literal historyLiteral : history)
				if (CheckEquals.Literal(historyLiteral, literal))
					return false;

			return true;
		}

		public CausalChain push(Literal literal) {
			CausalChain newCausalChain = clone();
			newCausalChain.history.add(0, literal);
			return newCausalChain;
		}

		public Literal head() {
			return history.get(0);
		}

		public Literal tail() {
			return history.get(history.size() - 1);
		}

		@Override
		public String toString() {
			return history.toString();
		}
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

}
