package sabre.logic;

import java.util.function.Function;

import sabre.Settings;
import sabre.Utilities;

public class NegatedLiteral extends Negation implements Literal {

	private static final long serialVersionUID = Settings.VERSION_UID;

	public final Literal argument;
	
	public NegatedLiteral(Literal argument) {
		super(argument);
		this.argument = argument;
	}

	@Override
	public NegatedLiteral apply(Function<Object, Object> function) {
		Literal argument = (Literal) function.apply(this.argument);
		if(this.argument != argument)
			return new NegatedLiteral(argument);
		else
			return this;			
	}
	
	@Override
	public Expression simplify() {
		if(argument instanceof Assignment && Utilities.isBoolean(((Assignment) argument).property))
			return argument.negate();
		else if(argument instanceof NegatedLiteral)
			return argument.negate();
		else
			return this;
	}
	
	@Override
	public Literal negate() {
		return argument;
	}
	
	@Override
	public DNFExpression toDNF() {
		return new DNFExpression(new ConjunctiveClause(this));
	}
	
	@Override
	public boolean matches(Literal other) {
		return !argument.matches(other);
	}
}
