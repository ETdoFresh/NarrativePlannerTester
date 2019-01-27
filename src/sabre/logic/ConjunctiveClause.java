package sabre.logic;

import sabre.Settings;
import sabre.util.ImmutableArray;

public class ConjunctiveClause extends Conjunction {

	private static final long serialVersionUID = Settings.VERSION_UID;

	public final ImmutableArray<? extends Literal> arguments;
	
	public ConjunctiveClause(ImmutableArray<? extends Literal> arguments) {
		super(arguments);
		this.arguments = arguments;
	}
	
	public ConjunctiveClause(Iterable<? extends Literal> arguments) {
		this(new ImmutableArray<>(arguments, Literal.class));
	}
	
	public ConjunctiveClause(Literal...arguments) {
		this(new ImmutableArray<>(arguments));
	}
	
	/*
	@Override
	public Expression simplify() {
		Expression simplified = super.simplify();
		if(simplified instanceof Conjunction) {
			ArrayList<Literal> literals = new ArrayList<>();
			for(Expression argument : ((Conjunction) simplified).arguments)
				literals.add((Literal) argument);
			simplified = new ConjunctiveClause(literals);
		}
		else if(simplified instanceof Literal)
			simplified = new ConjunctiveClause((Literal) simplified);
		return simplified;
	}
	*/
		
	@Override
	public DNFExpression toDNF() {
		return new DNFExpression(this);
	}
}
