package sabre.state;

import java.io.Serializable;

import sabre.Entity;
import sabre.Settings;
import sabre.State;
import sabre.logic.Expression;
import sabre.space.SearchSpace;
import sabre.space.Slot;

public class ArrayState implements State, Serializable {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	final SearchSpace space;
	final Entity[] values;
	
	ArrayState(SearchSpace space, Entity[] values) {
		this.space = space;
		this.values = values;
	}
	
	public ArrayState(SearchSpace space) {
		this.space = space;
		this.values = new Entity[space.slots.size()];
		for(Slot slot : space.slots)
			values[slot.id] = slot.initial;
	}
	
	public ArrayState(State other) {
		this(other.getSearchSpace());
		for(Slot slot : space.slots)
			values[slot.id] = other.get(slot);
	}
	
	public ArrayState(State previous, Expression expression) {
		MutableArrayState mutable = new MutableArrayState(previous);
		expression.impose(previous, mutable);
		this.space = mutable.space;
		this.values = mutable.values;
	}
	
	@Override
	public boolean equals(Object other) {
		return State.equals(this, other);
	}
	
	@Override
	public int hashCode() {
		return State.hashCode(this);
	}
	
	@Override
	public String toString() {
		return State.toString(this);
	}

	@Override
	public SearchSpace getSearchSpace() {
		return space;
	}

	@Override
	public Entity get(Slot slot) {
		return values[slot.id];
	}
}
