package sabre.logic;

import java.util.function.Function;

import sabre.Entity;
import sabre.MutableState;
import sabre.Settings;
import sabre.State;
import sabre.Utilities;
import sabre.space.Slot;

public class SlotAssignment extends Assignment {

	private static final long serialVersionUID = Settings.VERSION_UID;

	public final Slot slot;
	public final Entity value;
	
	public SlotAssignment(Slot slot, Entity value) {
		super(slot.property, slot.arguments, value);
		this.slot = slot;
		this.value = value;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof SlotAssignment) {
			SlotAssignment otherSA = (SlotAssignment) other;
			return slot == otherSA.slot && value == otherSA.value;
		}
		else
			return super.equals(other);
	}
	
	@Override
	public boolean isGround() {
		return true;
	}

	@Override
	public SlotAssignment apply(Function<Object, Object> function) {
		Slot slot = (Slot) function.apply(this.slot);
		Entity value = (Entity) function.apply(this.value);
		if(slot != this.slot || value != this.value)
			return new SlotAssignment(slot, value);
		else
			return this;
	}
	
	@Override
	public boolean test(State state) {
		return state.get(slot) == value;
	}
	
	@Override
	public boolean impose(State previous, MutableState state) {
		return state.set(slot, value);
	}
	
	@Override
	public Literal negate() {
		if(Utilities.isBoolean(property)) {
			if(value == Utilities.getFalse(property.domain))
				return new SlotAssignment(slot, Utilities.getTrue(property.domain));
			else if(value == Utilities.getTrue(property.domain))
				return new SlotAssignment(slot, Utilities.getFalse(property.domain));
		}
		return super.negate();
	}
}
