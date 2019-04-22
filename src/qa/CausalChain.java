package qa;

import java.io.Serializable;
import java.util.ArrayList;

import sabre.logic.Literal;

public class CausalChain implements Serializable {
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