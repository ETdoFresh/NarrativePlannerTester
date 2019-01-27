package sabre.logic;

import java.util.function.Function;

import sabre.MutableState;
import sabre.Settings;
import sabre.State;
import sabre.util.ImmutableArray;

public class Negation extends BooleanExpression {
	
	private static final long serialVersionUID = Settings.VERSION_UID;

	public final Expression argument;
	
	public Negation(Expression argument) {
		super(new ImmutableArray<>(new Expression[]{ argument }));
		this.argument = argument;
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof Negation) {
			Negation otherNegation = (Negation) other;
			return argument.equals(otherNegation.argument);
		}
		return false;
	}
	
	@Override
	public String toString() {
		String string = argument.toString();
		if(string.startsWith("("))
			return "!" + string;
		else
			return "!(" + string + ")";
	}
	
	@Override
	public Negation apply(Function<Object, Object> function) {
		Expression argument = (Expression) function.apply(this.argument);
		if(this.argument != argument)
			return new Negation(argument);
		else
			return this;			
	}
	
	@Override
	public boolean isTestable() {
		return argument.isTestable();
	}
	
	@Override
	public boolean test(State state) {
		return !argument.test(state);
	}
	
	@Override
	public boolean isImposable() {
		return argument.negate().isImposable();
	}
	
	@Override
	public boolean impose(State previous, MutableState state) {
		return argument.negate().impose(previous, state);
	}
	
	@Override
	public Expression simplify() {
		return argument.negate().simplify();
	}

	@Override
	public Expression negate() {
		return argument;
	}

	@Override
	public DNFExpression toDNF() {
		return argument.negate().toDNF();
	}
}
