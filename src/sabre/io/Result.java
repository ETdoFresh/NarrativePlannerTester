package sabre.io;

import sabre.util.ImmutableList;

final class Result {

	public final ParseTree tree;
	public final ImmutableList<Token> remainder;
	public final ParseException exception;
	
	Result(ParseTree tree, ImmutableList<Token> remainder, ParseException exception) {
		this.tree = tree;
		this.remainder = remainder;
		this.exception = exception == null ? getException(tree, remainder) : exception;
	}
	
	Result(ParseTree tree, ImmutableList<Token> remainder) {
		this(tree, remainder, null);
	}
	
	private static ParseException getException(ParseTree tree, ImmutableList<Token> remainder) {
		if(tree.tokens.size == 0 && remainder.size == 0)
			return new NothingException();
		else if(remainder.size == 0)
			return new ParseException("Expected something after \"" + Token.last(tree.tokens).first.content + "\".", Token.last(tree.tokens));
		else
			return new ParseException("Unexpected token \"" + remainder.first.content + "\".", Token.first(remainder));
	}
}
