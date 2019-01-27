package sabre.logic;

import java.util.function.Function;

import sabre.Typed;

public interface Term extends Logical, Typed {

	@Override
	public default Term apply(Function<Object, Object> function) {
		return this;
	}
}
