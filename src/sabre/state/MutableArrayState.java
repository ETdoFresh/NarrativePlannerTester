package sabre.state;

import sabre.Entity;
import sabre.MutableState;
import sabre.Settings;
import sabre.State;
import sabre.space.SearchSpace;
import sabre.space.Slot;

public class MutableArrayState extends ArrayState implements MutableState {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	MutableArrayState(SearchSpace space, Entity[] values) {
		super(space, values);
	}
	
	public MutableArrayState(State other) {
		super(other);
	}
	
	public MutableArrayState(SearchSpace space) {
		super(space);
	}

	@Override
	public boolean set(Slot slot, Entity value) {
		Entity before = values[slot.id];
		values[slot.id] = value;
		return before != value;
	}
}
