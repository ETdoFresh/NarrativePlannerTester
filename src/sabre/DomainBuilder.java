package sabre;

import java.util.ArrayList;

import sabre.logic.Expression;
import sabre.logic.ExpressionVariable;
import sabre.logic.TermVariable;
import sabre.logic.Variable;
import sabre.util.ImmutableArray;
import sabre.util.ImmutableSet;
import sabre.util.MutableArray;
import sabre.util.MutableSet;

public class DomainBuilder {

	private Domain domain;
	private String name;
	private String comment;
	private Expression goal;
	private String landmark;
	
	public DomainBuilder(Domain domain) {
		this.domain = new Domain(domain, domain.name, domain.comment, domain.goal);
		this.name = domain.name;
		this.comment = domain.comment;
		this.goal = domain.goal;
	}
	
	public Domain getDomain() {
		Domain newDomain = new Domain(domain, name, comment, goal);
		newDomain.landmark = landmark;
		return newDomain;
	}

	//-------------------------------------------------------------------------
	// Meta Information
	//-------------------------------------------------------------------------
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setLandmark(String landmark) {
		this.landmark = landmark;
		domain.landmark = landmark;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	//-------------------------------------------------------------------------
	// Types
	//-------------------------------------------------------------------------
	
	public void addType(String name, String comment) {
		if(domain.types.any(t -> t.name.equals(name)))
			apply(domain.types, t -> t.name.equals(name) ? new Type(domain, t.id, name, comment) : t);
		else {
			Type t = new Type(domain, domain.types.size(), name, comment);
			add(t.parents, domain.types.get(Settings.UNIVERSAL_SUPERTYPE_ID));
			add(domain.types, t);
		}
	}
	
	public void addTypeRelationship(String supertype, String subtype) {
		Type parent = domain.getType(supertype);
		Type child = domain.getType(subtype);
		if(parent == child)
			throw new FormatException("A type cannot extend itself.");
		else if(is(parent, child))
			throw new FormatException("Type \"" + subtype + "\" cannot extend \"" + supertype + "\" because \"" + supertype + "\" already extends \"" + subtype + "\".");
		add(child.parents, parent);
		sort(child.parents);
		checkAgents();
	}
	
	//-------------------------------------------------------------------------
	// Entities
	//-------------------------------------------------------------------------
		
	public void addEntity(String name, String comment) {
		if(domain.entities.any(e -> e.name.equals(name)))
			apply(domain.entities, e -> e.name.equals(name) ? new Entity(domain, e.id, name, comment) : e);
		else {
			Entity e = new Entity(domain, domain.entities.size(), name, comment);
			add(e.types, domain.types.get(Settings.UNIVERSAL_SUPERTYPE_ID));
			add(domain.entities, e);
		}
	}
	
	public void addEntityRelationship(String type, String entity) {
		Entity child = domain.getEntity(entity);
		Type parent = domain.getType(type);
		add(child.types, parent);
		sort(child.types);
		checkAgents();
	}
	
	//-------------------------------------------------------------------------
	// Properties
	//-------------------------------------------------------------------------
	
	public void addProperty(String name, String comment, String type, Iterable<String> parameters, String defaultValue) {
		ArrayList<Variable> variables = new ArrayList<>();
		for(String parameter : parameters)
			variables.add(new TermVariable(parameter, domain.getType(parameter)));
		Entity dv = defaultValue == null ? Utilities.getNull(domain) : domain.getEntity(defaultValue);
		Property property = new Property(domain, domain.properties.size(), name, comment, domain.getType(type), new ImmutableArray<>(variables, Variable.class), dv);
		// New property is more specific than an existing property.
		if(domain.properties.any(p -> isMoreGeneralThan(p, property))) {
			apply(domain.properties, p -> {
				if(isMoreGeneralThan(p, property))
					return new Property(domain, p.id, name, comment, p.type, p.parameters, getDefaultValue(p, property));
				else
					return p;
			});
		}
		// New property is more general than an existing property.
		else if(domain.properties.any(p -> isMoreGeneralThan(property, p))) {
			apply(domain.properties, p -> {
				if(isMoreGeneralThan(property, p))
					return new Property(domain, p.id, name, comment, property.type, property.parameters, getDefaultValue(p, property));
				else
					return p;
			});
		}
		// New property is distinct from existing properties.
		else
			add(domain.properties, property);
	}
	
	private static final boolean isMoreGeneralThan(Property p1, Property p2) {
		if(!p1.name.equals(p2.name))
			return false;
		else if(!is(p2.type, p1.type))
			return false;
		else if(p1.parameters.size() != p2.parameters.size())
			return false;
		for(int i=0; i<p1.parameters.size(); i++)
			if(!isMoreGeneralThan(p1.parameters.get(i), p2.parameters.get(i)))
				return false;
		return true;
	}
	
	private static final boolean isMoreGeneralThan(Variable v1, Variable v2) {
		if(v1 instanceof ExpressionVariable && v2 instanceof ExpressionVariable)
			return true;
		else if(v1 instanceof TermVariable && v2 instanceof TermVariable)
			return is(((TermVariable) v2).type, ((TermVariable) v1).type);
		else
			return false;
	}
	
	private static final Entity getDefaultValue(Property p1, Property p2) {
		if(Utilities.isNull(p1.defaultValue))
			return p2.defaultValue;
		else if(Utilities.isNull(p2.defaultValue))
			return p1.defaultValue;
		else if(p1.defaultValue == p2.defaultValue)
			return p1.defaultValue;
		else
			throw new FormatException("Cannot redefine the default value for property \"" + p1 + "\" from \"" + p1.defaultValue + "\" to \"" + p2.defaultValue + "\".");
	}
	
	//-------------------------------------------------------------------------
	// Events
	//-------------------------------------------------------------------------
	
	public void addAction(Action action) {
		add(domain.actions, action);
	}
	
	public void addAxiom(Axiom axiom) {
		add(domain.axioms, axiom);
	}
	
	//-------------------------------------------------------------------------
	// Initial State and Goal
	//-------------------------------------------------------------------------
	
	public void addToInitialState(Expression expression) {
		add(domain.initial, expression);
	}
	
	public void setGoal(Expression expression) {
		this.goal = expression;
	}
	
	//-------------------------------------------------------------------------
	// Utilities
	//-------------------------------------------------------------------------
	
	private static final boolean is(Type subtype, Type supertype) {
		if(subtype.name.equals(supertype.name))
			return true;
		for(Type parent : subtype.parents)
			if(is(parent, supertype))
				return true;
		return false;
	}
	
	private final boolean is(Entity entity, Type type) {
		for(Type parent : entity.types)
			if(is(parent, type))
				return true;
		return false;
	}
	
	private static final <T> void add(ImmutableArray<T> array, T object) {
		((MutableArray<T>) array).add(object);
	}
	
	private static final <T> void add(ImmutableSet<T> set, T object) {
		((MutableSet<T>) set).add(object);
	}
	
	private static final <T> void apply(ImmutableSet<T> set, java.util.function.Function<? super T, T> function) {
		((MutableSet<T>) set).apply(function);
	}
	
	private static final <U extends Unique> void sort(ImmutableSet<U> set) {
		((MutableSet<U>) set).sort(Utilities.SORT_BY_ID);
	}
	
	private final void checkAgents() {
		Type agentType = domain.types.get(Settings.AGENT_TYPE_ID);
		apply(domain.entities, entity -> {
			if(entity instanceof Entity && is(entity, agentType)) {
				Agent agent = new Agent(domain, entity.id, entity.name, entity.comment, domain.actions.size());
				for(Type type : entity.types)
					add(agent.types, type);
				add(domain.agents, agent);
				return agent;
			}
			else
				return entity;
		});
	}
}
