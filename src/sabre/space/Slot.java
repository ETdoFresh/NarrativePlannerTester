package sabre.space;

import java.io.Serializable;
import java.util.function.Function;

import sabre.Entity;
import sabre.Property;
import sabre.Settings;
import sabre.logic.Expression;
import sabre.logic.Logical;
import sabre.util.ImmutableArray;

public class Slot implements Serializable {
	
	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public final SearchSpace space;
	public final int id;
	public final Property property;
	public final ImmutableArray<Logical> arguments;
	public final Entity initial;
	
	public Slot(SearchSpace space, int id, Property property, ImmutableArray<Logical> arguments, Entity initial) {
		this.space = space;
		this.id = id;
		this.property = property;
		this.arguments = arguments;
		this.initial = initial;
	}
	
	@Override
	public String toString() {
		return sabre.Utilities.toFunctionString(property.name, arguments);
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	final Slot substitute(Function<Object, Object> substitution) {
		Property property = (Property) substitution.apply(this.property);
		Logical[] arguments = new Logical[this.arguments.size()];
		boolean different = false;
		for(int i=0; i<arguments.length; i++) {
			arguments[i] = this.arguments.get(i).substitute(substitution);
			if(arguments[i] != this.arguments.get(i)) {
				different = true;
				if(arguments[i] instanceof Expression)
					arguments[i] = ((Expression) arguments[i]).simplify().toDNF();
			}
		}
		Slot replacement = this;
		if(property != this.property || different)
			return new Slot(space, id, property, new ImmutableArray<>(arguments), initial);
		return (Slot) substitution.apply(replacement);
	}
}
