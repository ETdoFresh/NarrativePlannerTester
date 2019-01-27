package sabre.io;

import sabre.Settings;
import sabre.util.ImmutableList;

public class ParseException extends Exception {

	private static final long serialVersionUID = Settings.VERSION_UID;

	public final String message;
	public final ImmutableList<Token> tokens;
	
	public ParseException(String message, ImmutableList<Token> tokens) {
		super(getMessage(message, tokens));
		this.message = message;
		this.tokens = tokens;
	}
	
	private static final String getMessage(String message, ImmutableList<Token> tokens) {
		if(tokens.first == null)
			return message;
		else
			return message + " (line " + tokens.first.line + ")";
	}
}
