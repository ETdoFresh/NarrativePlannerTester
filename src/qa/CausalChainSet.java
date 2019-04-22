package qa;

import java.io.Serializable;
import java.util.ArrayList;

import sabre.Event;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Literal;

public class CausalChainSet implements Serializable {
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