package sabre;

import sabre.logic.Term;
import sabre.util.ImmutableSet;
import sabre.util.MutableSet;

public class Entity extends Unique implements Term {
	
	private static final long serialVersionUID = Settings.VERSION_UID;

	public final ImmutableSet<Type> types = new MutableSet<>(new Type[0]);
	
	Entity(Domain domain, int id, String name, String comment) {
		super(domain, id, name, comment);
	}
	
	Entity(DomainConstructor constructor, Entity toClone) {
		super(constructor.domain, toClone.id, toClone.name, toClone.comment);
		for(Type type : toClone.types)
			((MutableSet<Type>) this.types).add((Type) constructor.apply(type));
	}
	
	@Override
	public boolean isGround() {
		return true;
	}

	@Override
	public boolean is(Type type) {
		return domain.taxonomy.is(this, type);
	}
}
