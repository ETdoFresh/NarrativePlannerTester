package sabre.logic;

import java.util.function.Function;

import sabre.Domain;

public class QuantifierRemover implements Function<Object, Object> {

	private final Domain domain;
	private final Function<Object, Object> substitution;
	
	public QuantifierRemover(Domain domain, Function<Object, Object> substitution) {
		this.domain = domain;
		this.substitution = substitution;
	}
	
	public QuantifierRemover(Domain domain) {
		this(domain, o -> o);
	}
	
	@Override
	public Object apply(Object original) {
		if(original instanceof QuantifiedExpression)
			original = ((QuantifiedExpression) original).ground(domain);
		return substitution.apply(original);
	}
}
