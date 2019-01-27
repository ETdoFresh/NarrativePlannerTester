package sabre.io;

import sabre.util.ImmutableList;

public class Keyword extends Pattern {

	public final String keyword;
	
	public Keyword(String keyword) {
		this.keyword = keyword;
	}
	
	@Override
	public String toString() {
		return "keyword";
	}
	
	@Override
	Result match(Parser parser, ImmutableList<Path> path, ImmutableList<Token> tokens) throws ParseException {
		if(tokens.size() == 0)
			throw new NothingException();
		else if(tokens.first.content.equals(keyword))
			return new Result(new ParseTree(parser, Token.first(tokens), this), tokens.rest);
		else
			throw new ParseException("Expected \"" + keyword + "\" but encountered \"" + tokens.first.content + "\".", Token.first(tokens));
	}
	
	static final Builder KEYWORD_BUILDER = (tree) -> tree.tokens.first.content;
}
