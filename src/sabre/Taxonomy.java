package sabre;

import java.io.Serializable;

import sabre.util.CountableIterable;

public class Taxonomy implements Serializable {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	private final boolean[][] typeRelationships;
	private final boolean[][] entityRelationships;
	
	Taxonomy(CountableIterable<Type> types, CountableIterable<Entity> entities) {
		typeRelationships = new boolean[types.size()][types.size()];
		for(int i=0; i<types.size(); i++)
			typeRelationships[i][i] = true;
		for(Type child : types)
			for(Type parent : child.parents)
				typeRelationships[parent.id][child.id] = true;
		for(int i=0; i<types.size(); i++)
			for(int parent=0; parent<types.size(); parent++)
				for(int child=0; child<types.size(); child++)
					typeRelationships[parent][child] = typeRelationships[parent][child] || (typeRelationships[parent][i] && typeRelationships[i][child]);
		entityRelationships = new boolean[entities.size()][types.size()];
		for(Entity entity : entities)
			for(Type type : types)
				entityRelationships[entity.id][type.id] = entityIs(entity, type);
	}
	
	public boolean is(Type descendant, Type ancestor) {
		return typeRelationships[ancestor.id][descendant.id];
	}
	
	public boolean is(Entity entity, Type ancestor) {
		return entityRelationships[entity.id][ancestor.id];
	}
	
	private final boolean entityIs(Entity entity, Type ancestor) {
		for(Type type : entity.types)
			if(type == ancestor || is(type, ancestor))
				return true;
		return false;
	}
}
