package sabre.io;

import sabre.util.ImmutableList;

public class Token {

	public final String content;
	public final int line;
	public final int character;
	public final String comment;
	
	Token(String content, int line, int character, String comment) {
		this.content = content;
		this.line = line;
		this.character = character;
		this.comment = comment;
	}
	
	@Override
	public String toString() {
		return content;
	}
	
	static final String toString(ImmutableList<Token> tokens) {
		String string = "";
		for(Token token : tokens)
			string += token + " ";
		return string.trim();
	}
	
	static final <T> T compare(T o1, T o2) {
		if(o1 == null)
			return o2;
		else if(o2 == null)
			return o1;
		else if(score(o2) > score(o1))
			return o2;
		else
			return o1;
	}
	
	@SuppressWarnings("unchecked")
	private static final int score(Object object) {
		ImmutableList<Token> tokens;
		if(object instanceof Result)
			tokens = ((Result) object).remainder;
		else if(object instanceof ParseException)
			tokens = ((ParseException) object).tokens;
		else
			tokens = (ImmutableList<Token>) object;
		if(tokens.size() == 0)
			return 0;
		else
			return tokens.first.character + 1;
	}
	
	static final ImmutableList<Token> first(ImmutableList<Token> tokens) {
		return new ImmutableList<>(tokens.first);
	}
	
	static final ImmutableList<Token> last(ImmutableList<Token> tokens) {
		if(tokens.size <= 1)
			return tokens;
		else
			return last(tokens.rest);
	}
	
	static final ImmutableList<Token> subsequence(ImmutableList<Token> tokens, int length) {
		if(tokens.size() == 0 || length == 0)
			return new ImmutableList<>();
		else
			return new ImmutableList<>(tokens.first, subsequence(tokens.rest, length - 1));
	}
}
