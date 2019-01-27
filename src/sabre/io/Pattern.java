package sabre.io;

import sabre.util.ImmutableList;

public abstract class Pattern {
	
	Pattern() {}
	
	abstract Result match(Parser parser, ImmutableList<Path> path, ImmutableList<Token> tokens) throws ParseException;
	
	public static final Pattern toPattern(Object object) {
		if(object instanceof Pattern)
			return (Pattern) object;
		else if(object == null || object.equals(""))
			return Pattern.NOTHING;
		else if(object instanceof String)
			return new Keyword((String) object);
		else if(object.getClass().isArray()) {
			Object[] array = (Object[]) object;
			if(array.length == 0)
				return Pattern.NOTHING;
			return new Sequence(array);
		}
		else
			return new NonTerminal(object);
	}
	
	public static final Pattern NOTHING = new Pattern() {

		@Override
		public String toString() {
			return "nothing";
		}
		
		@Override
		Result match(Parser parser, ImmutableList<Path> path, ImmutableList<Token> tokens) throws ParseException {
			return new Result(new ParseTree(parser, new ImmutableList<>(), this), tokens);
		}
	};
	
	static final Builder NOTHING_BUILDER = (tree) -> null;
	
	public static final Pattern SYMBOL = new Pattern() {
		
		@Override
		public String toString() {
			return "symbol";
		}

		@Override
		Result match(Parser parser, ImmutableList<Path> path, ImmutableList<Token> tokens) throws ParseException {
			if(tokens.size() == 0)
				throw new NothingException();
			else if(tokens.first.content.matches("[A-Za-z0-9_\\-]+"))
				return new Result(new ParseTree(parser, Token.first(tokens), this), tokens.rest);
			else
				throw new ParseException("Expected symbol but encountered \"" + tokens.first.content + "\".", Token.first(tokens));
		}
	};
	
	static final Builder SYMBOL_BUILDER = (tree) -> tree.tokens.first.content;
	
	public static final Pattern STRING = new Pattern() {
		
		@Override
		public String toString() {
			return "string";
		}

		@Override
		Result match(Parser parser, ImmutableList<Path> path, ImmutableList<Token> tokens) throws ParseException {
			if(tokens.size() == 0)
				throw new NothingException();
			else if(tokens.first.content.startsWith("\"") && tokens.first.content.endsWith("\""))
				return new Result(new ParseTree(parser, Token.first(tokens), this), tokens.rest);
			else
				throw new ParseException("Expected string but encountered \"" + tokens.first.content + "\".", Token.first(tokens));
		}
	};
	
	static final Builder STRING_BUILDER = (tree) -> tree.tokens.first.content.substring(1, tree.tokens.first.content.length() - 1);
}
