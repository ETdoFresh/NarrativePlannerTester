package sabre.state;

import sabre.Entity;
import sabre.Settings;
import sabre.State;
import sabre.space.SearchSpace;
import sabre.space.Slot;

class ListStateRoot extends ListState {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	private final SearchSpace space;
	
	public ListStateRoot(SearchSpace space) {
		super((State) null);
		this.space = space;
	}
	
	@Override
	public SearchSpace getSearchSpace() {
		return space;
	}
	
	@Override
	public Entity get(Slot slot) {
		return slot.initial;
	}
}
