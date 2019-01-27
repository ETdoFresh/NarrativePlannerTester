package sabre.logic;

import java.util.function.Function;

import sabre.Settings;

public abstract class Variable implements Logical {

	private static final long serialVersionUID = Settings.VERSION_UID;

	public final String name;
	
	public Variable(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public final boolean isGround() {
		return false;
	}
	
	@Override
	public Variable apply(Function<Object, Object> function) {
		return this;
	}
}
