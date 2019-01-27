package sabre.logic;

import java.io.Serializable;
import java.util.function.Function;

public interface Logical extends Comparable<Logical>, Serializable {

	@Override
	public default int compareTo(Logical other) {
		if(this instanceof Negation) {
			Expression argument = ((Negation) this).argument;
			if(other instanceof Negation)
				return argument.compareTo(((Negation) other).argument);
			else if(argument.equals(other))
				return 1;
			else
				return argument.compareTo(other);
		}
		else if(other instanceof Negation) {
			Expression argument = ((Negation) this).argument;
			if(equals(argument))
				return -1;
			else
				return compareTo(argument);
		}
		else
			return toString().compareTo(other.toString());
	}
	
	public boolean isGround();
	
	public Logical apply(Function<Object, Object> function);
	
	public default Logical substitute(Function<Object, Object> substitution) {
		return (Logical) substitution.apply(apply(part -> {
			if(part instanceof Logical)
				return ((Logical) part).substitute(substitution);
			else
				return substitution.apply(part);
		}));
	}
}
