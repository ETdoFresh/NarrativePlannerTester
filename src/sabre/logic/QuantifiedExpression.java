package sabre.logic;

import java.util.ArrayList;
import java.util.function.Function;

import sabre.Domain;
import sabre.Entity;
import sabre.Settings;
import sabre.State;
import sabre.util.ImmutableArray;

public abstract class QuantifiedExpression implements Expression {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public final TermVariable variable;
	public final Expression argument;
	private final int hashCode;
	
	public QuantifiedExpression(TermVariable variable, Expression argument) {
		this.variable = variable;
		this.argument = argument;
		this.hashCode = (((getClass().hashCode() * 31) + variable.type.hashCode()) * 31) + argument.hashCode();
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	protected String toString(String quantifier) {
		String string = argument.toString();
		if(!string.startsWith("("))
			string = "(" + string + ")";
		return quantifier + "(" + variable.type + " " + variable + ") " + string;
	}

	@Override
	public boolean isGround() {
		return false;
	}
	
	@Override
	public boolean isTestable() {
		return true;
	}
	
	@Override
	public boolean test(State state) {
		return ground(state.getSearchSpace().domain).test(state);
	}
	
	@Override
	public Expression simplify() {
		return this;
	}

	@Override
	public DNFExpression toDNF() {
		throw new UnsupportedOperationException("Quantified expression \"" + this + "\" cannot be converted to disjunctive normal form.");
	}
	
	public abstract Expression ground(Domain domain);
	
	protected ImmutableArray<Expression> groundArguments(Domain domain) {
		ArrayList<Expression> arguments = new ArrayList<>();
		QuantifierSubstitution substitution = new QuantifierSubstitution(variable);
		for(Entity value : domain.getEntities(variable.type)) {
			substitution.value = value;
			arguments.add((Expression) argument.substitute(substitution));
		}
		return new ImmutableArray<>(arguments, Expression.class);
	}
	
	private static final class QuantifierSubstitution implements Function<Object, Object> {

		public final TermVariable variable;
		public Entity value;
		
		public QuantifierSubstitution(TermVariable variable) {
			this.variable = variable;
		}
		
		@Override
		public Object apply(Object original) {
			if(original.equals(variable))
				return value;
			else
				return original;
		}	
	}
}
