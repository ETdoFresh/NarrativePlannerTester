package sabre.logic;

import java.util.function.Function;

import sabre.Settings;
import sabre.util.ImmutableArray;

public abstract class BooleanExpression implements Expression {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public final ImmutableArray<? extends Expression> arguments;
	private final int hashCode;
	
	public BooleanExpression(ImmutableArray<? extends Expression> arguments) {
		this.arguments = arguments;
		this.hashCode = (getClass().hashCode() * 31) + arguments.hashCode();
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	protected final String toString(String operator) {
		String string = "";
		boolean first = true;
		for(Expression argument : arguments) {
			if(first)
				first = false;
			else
				string += " " + operator + " ";
			string += argument;
		}
		return string;
	}
	
	@Override
	public boolean isGround() {
		for(int i=0; i<arguments.size(); i++)
			if(!arguments.get(i).isGround())
				return false;
		return true;
	}
	
	@Override
	public abstract BooleanExpression apply(Function<Object, Object> function);
	
	@SuppressWarnings("unchecked")
	protected ImmutableArray<Expression> applyArguments(Function<Object, Object> function) {
		Expression[] applied = new Expression[arguments.size()];
		boolean different = false;
		for(int i=0; i<applied.length; i++) {
			applied[i] = (Expression) function.apply(arguments.get(i));
			different = different || arguments.get(i) != applied[i];
		}
		if(different)
			return new ImmutableArray<>(applied);
		else
			return (ImmutableArray<Expression>) arguments;
	}
	
	protected ImmutableArray<Expression> negateArguments() {
		Expression[] negated = new Expression[arguments.size()];
		for(int i=0; i<negated.length; i++)
			negated[i] = arguments.get(i).negate();
		return new ImmutableArray<>(negated);
	}
}
