package sabre.logic;

import sabre.Settings;
import sabre.util.ImmutableArray;

public class DNFExpression extends Disjunction {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public final ImmutableArray<? extends ConjunctiveClause> arguments;

	public DNFExpression(ImmutableArray<? extends ConjunctiveClause> arguments) {
		super(arguments);
		this.arguments = arguments;
	}
	
	public DNFExpression(Iterable<? extends ConjunctiveClause> arguments) {
		this(new ImmutableArray<>(arguments, ConjunctiveClause.class));
	}
	
	public DNFExpression(ConjunctiveClause...arguments) {
		this(new ImmutableArray<>(arguments));
	}
	
	/*
	@Override
	public Expression simplify() {
		Expression simplified = super.simplify();
		if(simplified instanceof Disjunction) {
			ArrayList<ConjunctiveClause> clauses = new ArrayList<>();
			for(Expression argument : ((Disjunction) simplified).arguments)
				clauses.add((ConjunctiveClause) argument);
			simplified = new DNFExpression(clauses);
		}
		return simplified;
	}
	*/
	
	@Override
	public DNFExpression toDNF() {
		return this;
	}
}
