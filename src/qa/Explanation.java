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

		for (ConjunctiveClause goal : goals.toDNF().arguments) {
			causalChainSet = new CausalChainSet(goal); // TODO remove this later and fix code to handle DNF
			causalChainMap.put(goal, causalChainSet);
		}
	}

	public boolean extend() {
		if (currentStepIndex < 0)
			return false;

		Action currentStep = plan.get(currentStepIndex).event;
		if (!causalChainLiteralExistsIn(currentStep.effect))
			return false;

		causalChainSet.addOrRemoveChainUsing(currentStep);
		currentStepIndex--;
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
		return true;
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
					for (CausalChain causalChain : causalChains)
						if (causalChain.head().equals(effectLiteral))
							if (causalChain.canPush(effectLiteral))
								return; // TODO continue this thought!
		}

		public boolean headContains(Literal literal) {
			for (CausalChain causalChain : causalChains)
				if (causalChain.head().equals(literal))
					return true;

			return false;
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

		public boolean canPush(Literal literal) {
			return !history.contains(literal);
		}

		public void push(Literal literal) {
			history.add(0, literal);
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
