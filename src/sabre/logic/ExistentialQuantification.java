package sabre.logic;

import java.util.function.Function;

import sabre.Domain;
import sabre.Settings;
import sabre.util.ImmutableArray;

public class ExistentialQuantification extends QuantifiedExpression {

	private static final long serialVersionUID = Settings.VERSION_UID;

	public ExistentialQuantification(TermVariable variable, Expression argument) {
		super(variable, argument);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof ExistentialQuantification) {
			ExistentialQuantification otherEQ = (ExistentialQuantification) other;
			return variable.equals(otherEQ.variable) && argument.equals(otherEQ.argument);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return toString("exists");
	}

	@Override
	public Expression apply(Function<Object, Object> function) {
		TermVariable variable = (TermVariable) function.apply(this.variable);
		Expression argument = (Expression) function.apply(this.argument);
		if(variable != this.variable || argument != this.argument)
			return new ExistentialQuantification(variable, argument);
		else
			return this;
	}

	@Override
	public Expression negate() {
		return new UniversalQuantification(variable, argument.negate());
	}

	@Override
	public Expression ground(Domain domain) {
		ImmutableArray<Expression> arguments = groundArguments(domain);
		if(arguments.size() == 0)
			return Expression.FALSE;
		else if(arguments.size() == 1)
			return arguments.get(0);
		else
			return new Disjunction(arguments);
	}
}
