package sabre;

import sabre.logic.Variable;
import sabre.util.ImmutableArray;
import sabre.util.MutableArray;

public class Property extends Unique {
	
	private static final long serialVersionUID = Settings.VERSION_UID;

	public final Type type;
	public final ImmutableArray<Variable> parameters;
	public final Entity defaultValue;
	
	public Property(Domain domain, int id, String name, String comment, Type type, ImmutableArray<Variable> parameters, Entity defaultValue) {
		super(domain, id, name, comment);
		this.type = type;
		this.parameters = parameters;
		this.defaultValue = defaultValue;
	}
	
	public Property(DomainConstructor constructor, Property toClone) {
		super(constructor.domain, toClone.id, toClone.name, toClone.comment);
		this.type = (Type) constructor.apply(toClone.type);
		this.parameters = new MutableArray<>(new Variable[0]);
		for(Variable parameter : toClone.parameters)
			((MutableArray<Variable>) this.parameters).add((Variable) constructor.apply(parameter));
		this.defaultValue = (Entity) constructor.apply(toClone.defaultValue);
	}
	
	@Override
	public String toString() {
		return Utilities.toFunctionString(name, parameters);
	}
}
