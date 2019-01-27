package sabre;

import sabre.space.Slot;

public interface MutableState extends State {

	public abstract boolean set(Slot slot, Entity value);
}
