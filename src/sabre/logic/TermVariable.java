package sabre.logic;

import java.util.function.Function;

import sabre.Settings;
import sabre.Type;

public class TermVariable extends Variable implements Term {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public final Type type;
	
	public TermVariable(String name, Type type) {
		super(name);
		this.type = type;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof TermVariable) {
			TermVariable otherTV = (TermVariable) other;
			return type == otherTV.type && name.equals(otherTV.name);
		}
		return false;
	}
	
	@Override
	public TermVariable apply(Function<Object, Object> function) {
		return this;
	}

	@Override
	public boolean is(Type type) {
		return this.type.is(type);
	}
}
