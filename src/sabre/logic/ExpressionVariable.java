package sabre.logic;

import java.util.function.Function;

import sabre.Settings;

public class ExpressionVariable extends Variable implements Literal {

	private static final long serialVersionUID = Settings.VERSION_UID;

	public ExpressionVariable(String name) {
		super(name);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof ExpressionVariable)
			return name.equals(((ExpressionVariable) other).name);
		else
			return false;
	}

	@Override
	public ExpressionVariable apply(Function<Object, Object> function) {
		return this;
	}

	@Override
	public Expression simplify() {
		return this;
	}
}
