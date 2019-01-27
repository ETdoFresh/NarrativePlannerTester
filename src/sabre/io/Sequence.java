package sabre.io;

import java.util.ArrayList;

import sabre.util.ImmutableList;

public class Sequence extends Pattern {

	private final Pattern[] parts;

	Sequence(Object...parts) {
		this.parts = new Pattern[parts.length];
		for(int i=0; i<parts.length; i++)
			this.parts[i] = Pattern.toPattern(parts[i]);
	}
	
	@Override
	public String toString() {
		return "sequence";
	}
	
	@Override
	Result match(Parser parser, ImmutableList<Path> path, ImmutableList<Token> tokens) throws ParseException {
		ArrayList<ParseTree> children = new ArrayList<>();
		ImmutableList<Token> current = tokens;
		ParseException exception = null;
		try {
			for(Pattern part : parts) {
				Result result = part.match(parser, path, current);
				if(!(part instanceof Keyword))
					children.add(result.tree);
				while(current != result.remainder)
					current = current.rest;
				exception = Token.compare(exception, result.exception);
			}
		}
		catch(ParseException ex) {
			throw Token.compare(ex, exception);
		}
		ImmutableList<Token> subsequence = Token.subsequence(tokens, tokens.size() - current.size());
		return new Result(new ParseTree(parser, children, subsequence, this), current, exception);
	}
}
