package sabre;

import java.io.Serializable;
import java.util.HashMap;
import java.util.TreeSet;

import sabre.logic.Expression;
import sabre.logic.ExpressionVariable;
import sabre.logic.TermVariable;
import sabre.logic.Variable;
import sabre.util.ImmutableArray;
import sabre.util.ImmutableSet;
import sabre.util.MutableArray;
import sabre.util.MutableSet;

public class Domain implements Serializable {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public final String name;
	public final String comment;
	public final ImmutableSet<Type> types = new MutableSet<>(new Type[0]);
	public final ImmutableSet<Entity> entities = new MutableSet<>(new Entity[0]);
	public final ImmutableSet<Agent> agents = new MutableSet<>(new Agent[0]);
	public final ImmutableSet<Property> properties = new MutableSet<>(new Property[0]);
	final Taxonomy taxonomy;
	public final ImmutableSet<Action> actions = new MutableSet<>(new Action[0]);
	public final ImmutableSet<Axiom> axioms = new MutableSet<>(new Axiom[0]);
	public final ImmutableArray<Expression> initial = new MutableArray<>(new Expression[0]);
	public final Expression goal;
	
	public Domain(String name, String comment) {
		this.name = name;
		this.comment = comment;
		Type universalSupertype = new Type(this, Settings.UNIVERSAL_SUPERTYPE_ID, Settings.UNIVERSAL_SUPERTYPE_NAME, "");
		add(types, universalSupertype);
		Type booleanType = new Type(this, Settings.BOOLEAN_TYPE_ID, Settings.BOOLEAN_TYPE_NAME, "");
		add(booleanType.parents, universalSupertype);
		add(types, booleanType);
		Type agentType = new Type(this, Settings.AGENT_TYPE_ID, Settings.AGENT_TYPE_NAME, "");
		add(agentType.parents, universalSupertype);
		add(types, agentType);
		Entity emptyEntity = new Entity(this, Settings.EMPTY_ENTITY_ID, Settings.EMPTY_ENTITY_NAME, "");
		add(emptyEntity.types, universalSupertype);
		add(emptyEntity.types, booleanType);
		add(entities, emptyEntity);
		Entity trueEntity = new Entity(this, Settings.BOOLEAN_TRUE_ID, Settings.BOOLEAN_TRUE_NAME, "");
		add(trueEntity.types, universalSupertype);
		add(trueEntity.types, booleanType);
		add(entities, trueEntity);
		Agent authorAgent = new Agent(this, Settings.AUTHOR_AGENT_ID, Settings.AUTHOR_AGENT_NAME, "", 0);
		add(authorAgent.types, universalSupertype);
		add(authorAgent.types, agentType);
		add(entities, authorAgent);
		add(agents, authorAgent);
		TermVariable agentParameter = new TermVariable(Settings.AGENT_TYPE_NAME, agentType);
		ExpressionVariable expressionParameter = new ExpressionVariable(Settings.EXPRESSION_VARIABLE_NAME);
		ImmutableArray<Variable> intentionParameters = new ImmutableArray<>(new Variable[]{ agentParameter, expressionParameter });
		Property intentionProperty = new Property(this, Settings.INTENTIONAL_PROPERTY_ID, Settings.INTENTIONAL_PROPERTY_NAME, "", booleanType, intentionParameters, emptyEntity);
		add(properties, intentionProperty);
		this.taxonomy = new Taxonomy(types, entities);
		this.goal = Expression.TRUE;
	}
	
	Domain(Domain toClone, String name, String comment, Expression goal) {
		this.name = name;
		this.comment = comment;
		DomainConstructor constructor = new DomainConstructor(this);
		// Types
		for(Type type : toClone.types)
			add(types, (Type) constructor.apply(type));
		// Entities
		for(Entity entity : toClone.entities)
			add(entities, (Entity) constructor.apply(entity));
		// Agents
		for(Entity entity : entities)
			if(entity instanceof Agent)
				add(agents, (Agent) entity);
		// Properties
		for(Property property : toClone.properties)
			add(properties, (Property) constructor.apply(property));
		// Taxonomy
		this.taxonomy = new Taxonomy(types, entities);
		// Actions
		for(Action action : toClone.actions)
			add(actions, action.substitute(constructor));
		// Axioms
		for(Axiom axiom : toClone.axioms)
			add(axioms, axiom.substitute(constructor));
		// Initial State
		for(Expression expression : toClone.initial)
			add(initial, (Expression) expression.substitute(constructor));
		// Goal
		this.goal = (Expression) goal.substitute(constructor);
	}
	
	private static final <T> void add(ImmutableArray<T> array, T object) {
		((MutableArray<T>) array).add(object);
	}
	
	private static final <T> void add(ImmutableSet<T> set, T object) {
		((MutableSet<T>) set).add(object);
	}
	
	@Override
	public String toString() {
		return "[Domain \"" + name + "\": " + types.size() + " types, " + properties.size() + " properties, " + entities.size() + " entities, " + actions.size() + " actions, " + axioms.size() + " axioms]";
	}

	public Type getType(String name) {
		for(Type type : types)
			if(type.name.equals(name))
				return type;
		throw new FormatException("Type \"" + name + "\" not defined.");
	}
	
	/*
	public Property getProperty(String type, String name, Iterable<String> parameters) {
		Type t = getType(type);
		ArrayList<Type> p = new ArrayList<>();
		for(String parameter : parameters)
			p.add(getType(parameter));
		for(Property property : properties)
			if(isMoreGeneralThan(property, t, name, p))
				return property;
		throw new FormatException("Property \"" + type + " : " + Utilities.toFunctionString(name, parameters) + "\" not defined.");
	}
	
	private static final boolean isMoreGeneralThan(Property property, Type type, String name, ArrayList<Type> parameters) {
		if(!type.is(property.type))
			return false;
		else if(!name.equals(property.name))
			return false;
		else if(parameters.size() != property.parameters.size())
			return false;
		for(int i=0; i<parameters.size(); i++)
			if(!parameters.get(i).is(property.parameters.get(i)))
				return false;
		return true;
	}
	*/
	
	public Entity getEntity(String name) {
		for(Entity entity : entities)
			if(entity.name.equals(name))
				return entity;
		if(name.equals(Settings.BOOLEAN_FALSE_NAME))
			return entities.get(Settings.BOOLEAN_FALSE_ID);
		throw new FormatException("Entity \"" + name + "\" not defined.");
	}
	
	public Agent getAgent(String name) {
		for(Agent agent : agents)
			if(agent.name.equals(name))
				return agent;
		throw new FormatException("Agent \"" + name + "\" not defined.");
	}
	
	private final HashMap<Type, ImmutableSet<Entity>> entitiesByType = new HashMap<>();
	
	public ImmutableSet<Entity> getEntities(Type type) {
		ImmutableSet<Entity> entities = entitiesByType.get(type);
		if(entities == null) {
			TreeSet<Entity> set = new TreeSet<>(Utilities.SORT_BY_ID);
			for(Entity entity : this.entities)
				if(entity.is(type))
					set.add(entity);
			entities = new ImmutableSet<>(set, Entity.class);
			entitiesByType.put(type, entities);
		}
		return entities;
	}
	
	public Action getAction(String name) {
		for(Action action : actions)
			if(action.name.equals(name))
				return action;
		throw new FormatException("Action \"" + name + "\" not defined.");
	}
	
	public Axiom getAxiom(String name) {
		for(Axiom axiom : axioms)
			if(axiom.name.equals(name))
				return axiom;
		throw new FormatException("Axiom \"" + name + "\" not defined.");
	}
}
