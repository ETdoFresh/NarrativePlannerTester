package sabre;

import java.util.ArrayList;
import java.util.function.Function;

import sabre.logic.TermVariable;

class DomainConstructor implements Function<Object, Object> {

	public final Domain domain;
	private final ArrayList<Unique> objects = new ArrayList<>();
	
	public DomainConstructor(Domain domain) {
		this.domain = domain;
	}
	
	@Override
	public Object apply(Object input) {
		if(input instanceof Unique) {
			int id = ((Unique) input).id;
			for(Unique unique : objects)
				if(unique.getClass() == input.getClass() && unique.id == id)
					return unique;
		}
		Object result;
		if(input instanceof Type)
			result = new Type(this, (Type) input);
		else if(input instanceof Property)
			result = new Property(this, (Property) input);
		else if(input instanceof Agent)
			result = new Agent(this, (Agent) input);
		else if(input instanceof Entity)
			result = new Entity(this, (Entity) input);
		else if(input instanceof TermVariable) {
			TermVariable tv = (TermVariable) input;
			result = new TermVariable(tv.name, (Type) apply(tv.type));
		}
		else
			result = input;
		if(result instanceof Unique)
			objects.add((Unique) result);
		return result;
	}
}
