package sabre.state;

import sabre.Entity;
import sabre.MutableState;
import sabre.State;
import sabre.logic.Expression;
import sabre.space.SearchSpace;
import sabre.space.Slot;

public class MutableListState implements MutableState {

	State current;
	
	public MutableListState(State current) {
		this.current = current;
	}
	
	public MutableListState(SearchSpace space) {
		this.current = new ListStateRoot(space);
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
		return current.getSearchSpace();
	}

	@Override
	public Entity get(Slot slot) {
		return current.get(slot);
	}

	@Override
	public boolean set(Slot slot, Entity value) {
		Entity before = get(slot);
		if(before == value)
			return false;
		else {
			current = new ListStateValue(current, slot, value);
			return true;
		}
	}
	
	public boolean impose(Expression expression) {
		return expression.impose(current, this);
	}
}
