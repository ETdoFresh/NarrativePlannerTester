package sabre;

import java.io.Serializable;

import sabre.util.ImmutableSet;
import sabre.util.MutableSet;

public class Type extends Unique implements Typed, Serializable {
	
	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public final ImmutableSet<Type> parents = new MutableSet<>(new Type[0]);
	
	Type(Domain domain, int id, String name, String comment) {
		super(domain, id, name, comment);
	}
	
	Type(DomainConstructor constructor, Type toClone) {
		super(constructor.domain, toClone.id, toClone.name, toClone.comment);
		for(Type parent : toClone.parents)
			((MutableSet<Type>) this.parents).add((Type) constructor.apply(parent));
	}
	
	@Override
	public boolean is(Type ancestor) {
		return domain.taxonomy.is(this, ancestor);
	}
}
