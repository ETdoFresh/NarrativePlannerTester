package sabre.io;

import sabre.Settings;
import sabre.util.ImmutableList;

public class LeftRecursiveException extends IllegalStateException {

	private static final long serialVersionUID = Settings.VERSION_UID;

	public LeftRecursiveException(ImmutableList<Path> path) {
		super("Parser has entered an infinite loop: " + toString(path) + ".");
	}
	
	private static final String toString(ImmutableList<Path> path) {
		String string = path.first.pattern.toString();
		path = path.rest;
		for(Path p : path)
			string = p.pattern + " ::= " + string;
		return string;
	}
}
