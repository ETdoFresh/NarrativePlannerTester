package sabre.io;

import java.util.ArrayList;

import sabre.util.ImmutableList;

public class Rule {

	public final Object left;
	public final Pattern right;
	
	public Rule(Object left, Object...right) {
		this.left = NonTerminal.key(left);
		if(right.length == 1)
			this.right = Pattern.toPattern(right[0]);
		else
			this.right = Pattern.toPattern(right);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Rule) {
			Rule otherRule = (Rule) other;
			return left.equals(otherRule.left) && right.equals(otherRule.right);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return left + " ::= " + right;
	}
	
	final Result apply(Parser parser, ImmutableList<Path> path, ImmutableList<Token> tokens) throws ParseException {
		Result result = right.match(parser, path, tokens);
		if(result.tree.type instanceof Sequence)
			return new Result(new ParseTree(parser, result.tree, result.tree.tokens, left), result.remainder, result.exception);
		else {
			ArrayList<ParseTree> child = new ArrayList<>();
			child.add(result.tree);
			return new Result(new ParseTree(parser, child, result.tree.tokens, left), result.remainder, result.exception);
		}
	}
}
