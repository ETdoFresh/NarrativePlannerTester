package qa;

import java.util.ArrayList;
import java.util.HashMap;

import sabre.Action;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Expression;
import sabre.logic.Literal;
import sabre.util.ImmutableArray;

public class Explanation {
	public RelaxedPlan plan;
	public InitialState initial;
	public Expression goals;
	public HashMap<ConjunctiveClause, CausalChainSet> causalChainMap = new HashMap<>();
	public CausalChainSet causalChainSet;
	public int currentStepIndex = -1;

	public Explanation(RelaxedPlan plan, ImmutableArray<Expression> initial, Expression goals) {
		this.plan = plan;
		this.goals = goals;
		this.initial = new InitialState(initial);
		this.currentStepIndex = plan.size() - 1;
	}

	public boolean build() {
		if (currentStepIndex < 0)
			return false;

		// Create Initial Causal Chains
		for (ConjunctiveClause goal : goals.toDNF().arguments) {
			causalChainSet = new CausalChainSet(goal); // TODO remove this later and fix code to handle DNF
			causalChainMap.put(goal, causalChainSet);
		}

		// Working backwards on RelaxedPlan, Find first step that has an effect that
		// contains one (or more) causal chain heads
		while (currentStepIndex >= 0) {
			Action currentStep = plan.get(currentStepIndex).event;
			if (!causalChainLiteralExistsIn(currentStep.effect)) {
				currentStepIndex--;
			} else {
				causalChainSet.addOrRemoveChainUsing(currentStep);
				currentStepIndex--;
			}
		}
		return initialStateContainsAllCausalChainHeads();
	}

	private boolean initialStateContainsAllCausalChainHeads() {
		for (Literal head : causalChainSet.heads())
			if (!initial.contains(head))
				return false;

		return true;
	}

	private boolean causalChainLiteralExistsIn(Expression effect) {
		for (ConjunctiveClause e : effect.toDNF().arguments)
			for (Literal l : e.arguments)
				if (causalChainSet.headContains(l))
					return true;
		return false;
	}

	public static boolean IsValid(RelaxedPlan plan, ImmutableArray<Expression> initial, Expression goals) {
		Explanation explanation = new Explanation(plan, initial, goals);
		return explanation.build();
	}

	public class CausalChainSet {
		ArrayList<CausalChain> causalChains = new ArrayList<>();

		public CausalChainSet(ConjunctiveClause goal) {
			for (Literal goalLiteral : goal.arguments)
				causalChains.add(new CausalChain(goalLiteral));
		}

		public void addOrRemoveChainUsing(Action currentStep) {
			for (ConjunctiveClause e : currentStep.effect.toDNF().arguments)
				for (Literal effectLiteral : e.arguments)
					for (int i = causalChains.size() - 1; i >= 0; i--) {
						CausalChain causalChain = causalChains.get(i);
						if (causalChain.head().equals(effectLiteral)) {
							for (ConjunctiveClause p : currentStep.precondition.toDNF().arguments)
								for (Literal preconditionLiteral : p.arguments)
									if (causalChain.canPush(preconditionLiteral))
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
			for (CausalChain chain : causalChains)
				output += chain + "\n";
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

		public boolean canPush(Literal literal) {
			return !history.contains(literal);
		}

		public CausalChain push(Literal literal) {
			CausalChain newCausalChain = new CausalChain(history);
			newCausalChain.history.add(0, literal);
			return newCausalChain;
		}

		public Literal head() {
			return history.get(0);
		}

		@Override
		public String toString() {
			return history.toString();
		}
	}

	private class InitialState {
		ImmutableArray<Expression> initial;

		public InitialState(ImmutableArray<Expression> initial) {
			this.initial = initial;
		}

		public boolean contains(Literal literal) {
			for (Expression expression : initial)
				for (ConjunctiveClause clause : expression.toDNF().arguments)
					if (clause.arguments.contains(literal))
						return true;

			return false;
		}
	}
}
