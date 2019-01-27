package sabre.state;

import sabre.Entity;
import sabre.Settings;
import sabre.State;
import sabre.space.Slot;

class ListStateValue extends ListState {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	private final Slot slot;
	private final Entity value;
	
	public ListStateValue(State previous, Slot slot, Entity value) {
		super(previous);
		this.slot = slot;
		this.value = value;
	}
	
	@Override
	public Entity get(Slot slot) {
		if(slot == this.slot)
			return value;
		else
			return super.get(slot);
	}
}
