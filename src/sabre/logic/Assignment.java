package sabre.logic;

import java.util.function.Function;

import sabre.Entity;
import sabre.FormatException;
import sabre.MutableState;
import sabre.Property;
import sabre.Settings;
import sabre.State;
import sabre.Utilities;
import sabre.util.ImmutableArray;

public class Assignment implements Literal {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public final Property property;
	public final ImmutableArray<Logical> arguments;
	public final Term value;
	private final int hashCode;
	
	public Assignment(Property property, ImmutableArray<Logical> arguments, Term value) {
		this.property = property;
		if(property.parameters.size() != arguments.size())
			throw new FormatException("Property \"" + property.name + "\" expects " + property.parameters.size() + " arguments, but " + arguments.size() + " were given.");
		for(int i=0; i<property.parameters.size(); i++) {
			if(property.parameters.get(i) instanceof ExpressionVariable) {
				if(!(arguments.get(i) instanceof Expression))
					throw new FormatException("Argument \"" + arguments.get(i) + "\" is not an expression.");
			}
			else {
				TermVariable parameter = (TermVariable) property.parameters.get(i);
				if(!(arguments.get(i) instanceof Term))
					throw new FormatException("Argument \"" + arguments.get(i) + "\" is not a term.");
				if(!((Term) arguments.get(i)).is(parameter.type))
					throw new FormatException("Argument \"" + arguments.get(i) + "\" must be of type \"" + parameter.type + "\".");		
			}
		}
		this.arguments = arguments;
		if(!(Utilities.isNull(value) || value.is(property.type)))
			throw new FormatException("Value \"" + value + "\" must be of type \"" + property.type + "\".");
		this.value = value;
		int hc = property.hashCode();
		for(Logical argument : arguments)
			hc = (hc * 31) + argument.hashCode();
		this.hashCode = (hc * 31) + value.hashCode();
	}
	
	public Assignment(Property property, Iterable<Logical> arguments, Term value) {
		this(property, new ImmutableArray<>(arguments, Logical.class), value);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Assignment) {
			Assignment otherAssignment = (Assignment) other;
			return property == otherAssignment.property && arguments.equals(otherAssignment.arguments) && value.equals(otherAssignment.value);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public String toString() {
		return Utilities.toFunctionString(property.name, arguments) + " = " + value;
	}
	
	@Override
	public boolean isGround() {
		for(int i=0; i<arguments.size(); i++)
			if(!arguments.get(i).isGround())
				return false;
		return value.isGround();
	}
	
	@Override
	public Assignment apply(Function<Object, Object> function) {
		Property property = (Property) function.apply(this.property);
		Logical[] arguments = new Logical[this.arguments.size()];
		boolean different = false;
		for(int i=0; i<this.arguments.size(); i++) {
			arguments[i] = (Logical) function.apply(this.arguments.get(i));
			different = different || arguments[i] != this.arguments.get(i);
		}
		Term value = (Term) function.apply(this.value);
		if(property != this.property || different || value != this.value)
			return new Assignment(property, new ImmutableArray<>(arguments), value);
		else
			return this;
	}
	
	@Override
	public boolean isTestable() {
		return true;
	}

	@Override
	public boolean test(State state) {
		if(isGround())
			return state.get(state.getSearchSpace().getSlot(property, arguments)) == value;
		else
			throw new IllegalStateException("Cannot test \"" + this + "\" because it is not ground.");
	}
	
	@Override
	public boolean isImposable() {
		return true;
	}

	@Override
	public boolean impose(State previous, MutableState state) {
		if(isGround())
			return state.set(state.getSearchSpace().getSlot(property, arguments), (Entity) value);
		else
			throw new IllegalStateException("Cannot impose \"" + this + "\" because it is not ground.");
	}
	
	@Override
	public Expression simplify() {
		return this;
	}
	
	@Override
	public Literal negate() {
		if(Utilities.isBoolean(property)) {
			if(value == Utilities.getFalse(property.domain))
				return new Assignment(property, arguments, Utilities.getTrue(property.domain));
			else if(value == Utilities.getTrue(property.domain))
				return new Assignment(property, arguments, Utilities.getFalse(property.domain));
		}
		return new NegatedLiteral(this);
	}
}
