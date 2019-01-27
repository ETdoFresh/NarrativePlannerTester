package sabre.logic;

import java.util.function.Function;

public interface Literal extends Expression {

	@Override
	public Literal apply(Function<Object, Object> function);
	
	@Override
	public default Literal negate() {
		return new NegatedLiteral(this);
	}
	
	@Override
	public default DNFExpression toDNF() {
		return new DNFExpression(new ConjunctiveClause(this));
	}
	
	public default boolean matches(Literal other) {
		return equals(other);
	}
}
