package sabre.logic;

import java.util.function.Function;

import sabre.Domain;
import sabre.MutableState;
import sabre.Settings;
import sabre.State;
import sabre.util.ImmutableArray;

public class UniversalQuantification extends QuantifiedExpression {

	private static final long serialVersionUID = Settings.VERSION_UID;

	public UniversalQuantification(TermVariable variable, Expression argument) {
		super(variable, argument);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof UniversalQuantification) {
			UniversalQuantification otherUQ = (UniversalQuantification) other;
			return variable.equals(otherUQ.variable) && argument.equals(otherUQ.argument);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return toString("forall");
	}

	@Override
	public UniversalQuantification apply(Function<Object, Object> function) {
		TermVariable variable = (TermVariable) function.apply(this.variable);
		Expression argument = (Expression) function.apply(this.argument);
		if(variable != this.variable || argument != this.argument)
			return new UniversalQuantification(variable, argument);
		else
			return this;
	}
	
	@Override
	public boolean isImposable() {
		return true;
	}
	
	@Override
	public boolean impose(State previous, MutableState state) {
		return ground(state.getSearchSpace().domain).impose(previous, state);
	}

	@Override
	public Expression negate() {
		return new ExistentialQuantification(variable, argument.negate());
	}

	@Override
	public Expression ground(Domain domain) {
		ImmutableArray<Expression> arguments = groundArguments(domain);
		if(arguments.size() == 0)
			return Expression.TRUE;
		else if(arguments.size() == 1)
			return arguments.get(0);
		else
			return new Conjunction(arguments);
	}
}
