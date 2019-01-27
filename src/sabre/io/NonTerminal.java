package sabre.io;

import sabre.util.ImmutableList;

public class NonTerminal extends Pattern {

	public final Object key;
	
	public NonTerminal(Object key) {
		if(key instanceof NonTerminal)
			this.key = ((NonTerminal) key).key;
		else
			this.key = key;
	}
	
	static final Object key(Object object) {
		if(object instanceof NonTerminal)
			return ((NonTerminal) object).key;
		else
			return object;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof NonTerminal)
			return key.equals(((NonTerminal) other).key);
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	@Override
	public String toString() {
		if(key instanceof Class)
			return ((Class<?>) key).getSimpleName();
		else
			return key.toString();
	}

	@Override
	Result match(Parser parser, ImmutableList<Path> path, ImmutableList<Token> tokens) throws ParseException {
		Path extension = new Path(this, tokens);
		ImmutableList<Path> extended = path.add(extension);
		if(path.contains(extension))
			throw new LeftRecursiveException(extended);
		else
			path = extended;
		ParseException best = null;
		for(Rule rule : parser.rules) {
			if(rule.left == key) {
				try {
					return rule.apply(parser, path, tokens);
				}
				catch(ParseException ex) {
					best = Token.compare(best, ex);
				}
			}
		}
		if(best == null)
			throw new ParseException("Failed to parse " + this + ".", tokens);
		else
			throw best;
	}
}
