package sabre;

import java.util.function.Function;

import sabre.logic.Expression;
import sabre.logic.Logical;
import sabre.util.ImmutableArray;

public class Axiom extends Event {
	
	private static final long serialVersionUID = Settings.VERSION_UID;

	public Axiom(String name, String comment, ImmutableArray<Logical> arguments, Expression precondition, Expression effect) {
		super(name, comment, arguments, precondition, effect);
	}
	
	protected Axiom(Axiom toClone, Function<Object, Object> substitution) {
		super(toClone, substitution);
	}
	
	public Axiom substitute(Function<Object, Object> substitution) {
		return new Axiom(this, substitution);
	}
}
