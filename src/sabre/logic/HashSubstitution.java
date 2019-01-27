package sabre.logic;

import java.util.HashMap;
import java.util.function.Function;

public class HashSubstitution implements Function<Object, Object> {

	private final HashMap<Object, Object> substitution = new HashMap<>();
	
	@Override
	public Object apply(Object original) {
		if(substitution.containsKey(original)) {
			Object substitution = this.substitution.get(original);
			if(original == substitution)
				return original;
			else
				return apply(substitution);
		}
		else
			return original;
	}
	
	public void set(Object original, Object substitution) {
		if(original == substitution)
			this.substitution.remove(original);
		else
			this.substitution.put(original, substitution);
	}
}
