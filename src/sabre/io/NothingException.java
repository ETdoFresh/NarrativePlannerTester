package sabre.io;

import sabre.Settings;
import sabre.util.ImmutableList;

final class NothingException extends ParseException {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public NothingException() {
		super("Expected something but encountered nothing.", new ImmutableList<Token>());
	}
}
