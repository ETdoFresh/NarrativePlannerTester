package sabre.io;

import java.util.ArrayList;

import sabre.util.ImmutableList;

public class List extends Pattern {

	private static final int NO_MAX = -1;
	
	private final Pattern separator;
	private final Pattern content;
	private final int min;
	private final int max;
	
	public List(Object content, String separator, int min, int max) {
		this.separator = Pattern.toPattern(separator);
		this.content = Pattern.toPattern(content);
		if(this.content == Pattern.NOTHING)
			throw new IllegalArgumentException("Content cannot be nothing.");
		this.min = min;
		this.max = max;
	}
	
	public List(Object content, String separator, int min) {
		this(content, separator, min, NO_MAX);
	}
	
	public List(Object content, String separator) {
		this(content, separator, 0);
	}
	
	@Override
	public String toString() {
		return "list";
	}
	
	private static final ImmutableList<Token> NO_END = new ImmutableList<>();

	@Override
	Result match(Parser parser, ImmutableList<Path> path, ImmutableList<Token> tokens) throws ParseException {
		ImmutableList<Token> current = tokens;
		ArrayList<ParseTree> children = new ArrayList<>();
		ParseException exception = null;
		try {
			boolean loop = true;
			while(loop) {
				ImmutableList<Token> end;
				ImmutableList<Token> start;
				if(separator == Pattern.NOTHING) {
					end = NO_END;
					start = current;
				}
				else {
					end = find(((Keyword) separator).keyword, current, parser);
					if(end.size() == 0 && children.size() + 1 < min)
						throw new ParseException("At least " + min + " parts are required.", Token.subsequence(tokens, tokens.size() - current.size()));
					else if(end.size() == 0)
						start = current;
					else
						start = Token.subsequence(current, current.size() - end.size());
				}
				Result result = content.match(parser, path, start);
				children.add(result.tree);
				if(end.size() == 0)
					current = result.remainder;
				else if(result.remainder.size() == 0)
					current = end;
				else
					while(current.first != result.remainder.first)
						current = current.rest;
				exception = Token.compare(exception, result.exception);
				try {
					result = separator.match(parser, path, current);
					current = result.remainder;
				}
				catch(ParseException ex) {
					loop = false;
				}
			}
		}
		catch(ParseException ex) {
			exception = Token.compare(exception, ex);
		}
		ImmutableList<Token> subsequence = Token.subsequence(tokens, tokens.size() - current.size());
		if(children.size() < min)
			throw new ParseException("At least " + min + " parts are required.", subsequence);
		if(max != NO_MAX && children.size() > max)
			throw new ParseException("No more than " + max + " parts are allowed.", subsequence);
		return new Result(new ParseTree(parser, children, subsequence, this), current, exception);
	}
	
	private static final ImmutableList<Token> find(String string, ImmutableList<Token> tokens, Parser parser) {
		ImmutableList<String> stack = new ImmutableList<>();
		while(tokens.size() > 0) {
			if(stack.size() > 0 && stack.first.equals(tokens.first.content))
				stack = stack.rest;
			else if(stack.size() == 0 && tokens.first.content.equals(string))
				return tokens;
			else {
				String right = parser.getRightBracket(tokens.first.content);
				if(right != null)
					stack = stack.add(right);
			}
			tokens = tokens.rest;
		}
		return tokens;
	}
	
	static final Builder LIST_BUILDER = new Builder() {

		@Override
		public Object build(ParseTree tree) throws ParseException {
			ArrayList<Object> children = new ArrayList<>();
			for(ParseTree child : tree)
				children.add(child.build(Object.class));
			return children.toArray(new Object[children.size()]);
		}
	};
}
