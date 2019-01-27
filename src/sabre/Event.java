package sabre;

import java.io.Serializable;
import java.util.function.Function;

import sabre.logic.Expression;
import sabre.logic.Logical;
import sabre.util.ImmutableArray;
import sabre.util.MutableArray;

public abstract class Event implements Comparable<Event>, Serializable {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public final String name;
	public final String comment;
	public final ImmutableArray<Logical> arguments;
	public final Expression precondition;
	public final Expression effect;
	private final int hashCode;
	
	public Event(String name, String comment, ImmutableArray<Logical> arguments, Expression precondition, Expression effect) {
		this.name = name;
		this.comment = comment;
		this.arguments = arguments;
		if(!precondition.isTestable())
			throw new FormatException("Precondition must be testable.");
		this.precondition = precondition;
		if(!effect.isImposable())
			throw new FormatException("Effect must be imposable.");
		this.effect = effect;
		this.hashCode = hashCode(this);
	}
	
	protected Event(Event toClone, Function<Object, Object> substitution) {
		this.name = (String) substitution.apply(toClone.name);
		this.comment = (String) substitution.apply(toClone.comment);
		this.arguments = new MutableArray<>(new Logical[0]);
		for(Logical argument : toClone.arguments)
			((MutableArray<Logical>) arguments).add(argument.substitute(substitution));
		this.precondition = (Expression) toClone.precondition.substitute(substitution);
		this.effect = (Expression) toClone.effect.substitute(substitution);
		this.hashCode = hashCode(this);
	}
	
	private static final int hashCode(Event event) {
		return (event.name.hashCode() * 31) + event.arguments.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if(getClass().isAssignableFrom(other.getClass()) || other.getClass().isAssignableFrom(getClass())) {
			Event otherEvent = (Event) other;
			if(name.equals(otherEvent.name) && arguments.size() == otherEvent.arguments.size()) {
				for(int i=0; i<arguments.size(); i++)
					if(!arguments.get(i).equals(otherEvent.arguments.get(i)))
						return false;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public String toString() {
		return Utilities.toFunctionString(name, arguments);
	}
	
	@Override
	public int compareTo(Event other) {
		int result = name.compareTo(other.name);
		if(result == 0)
			result = arguments.size() - other.arguments.size();
		for(int i=0; result==0 && i<arguments.size(); i++)
			result = arguments.get(i).compareTo(other.arguments.get(i));
		return result;
	}
	
	public abstract Event substitute(Function<Object, Object> function);
}
