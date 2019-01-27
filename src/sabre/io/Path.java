package sabre.io;

import sabre.util.ImmutableList;

final class Path {
	
	public final NonTerminal pattern;
	public final ImmutableList<Token> tokens;
	
	Path(NonTerminal pattern, ImmutableList<Token> tokens) {
		this.pattern = pattern;
		this.tokens = tokens;
	}
	
	@Override
	public String toString() {
		return pattern.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Path) {
			Path otherPath = (Path) other;
			return pattern == otherPath.pattern && tokens == otherPath.tokens;
		}
		return false;
	}
}
