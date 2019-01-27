package sabre.logic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.function.Function;

import sabre.MutableState;
import sabre.Settings;
import sabre.State;
import sabre.util.ImmutableArray;

public class Disjunction extends BooleanExpression {

	private static final long serialVersionUID = Settings.VERSION_UID;

	public Disjunction(ImmutableArray<? extends Expression> arguments) {
		super(arguments);
	}
	
	public Disjunction(Iterable<? extends Expression> arguments) {
		this(new ImmutableArray<>(arguments, Expression.class));
	}
	
	public Disjunction(Expression...arguments) {
		this(new ImmutableArray<>(arguments));
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Disjunction)
			return arguments.equals(((Disjunction) other).arguments);
		return false;
	}
	
	@Override
	public String toString() {
		return toString("|");
	}
	
	@Override
	public Disjunction apply(Function<Object, Object> function) {
		ImmutableArray<Expression> applied = applyArguments(function);
		if(applied == arguments)
			return this;
		else
			return new Disjunction(applyArguments(function));
	}
	
	@Override
	public boolean isTestable() {
		return true;
	}
	
	@Override
	public boolean test(State state) {
		for(int i=0; i<arguments.size(); i++)
			if(arguments.get(i).test(state))
				return true;
		return false;
	}
	
	@Override
	public boolean isImposable() {
		return arguments.size() == 1;
	}
	
	@Override
	public boolean impose(State previous, MutableState state) {
		if(arguments.size() == 1)
			return arguments.get(0).impose(previous, state);
		else
			return super.impose(previous, state);
	}
	
	@Override
	public Expression simplify() {
		LinkedHashSet<Expression> simplified = new LinkedHashSet<>();
		boolean different = false;
		for(Expression argument : arguments) {
			Expression s = argument.simplify();
			if(argument != s)
				different = true;
			if(s == Expression.TRUE)
				return Expression.TRUE;
			else if(s == Expression.FALSE)
				continue;
			else
				simplified.add(s);
		}
		if(simplified.size() == 0)
			return Expression.FALSE;
		else if(simplified.size() == 1)
			return simplified.iterator().next();
		else if(different || simplified.size() != arguments.size())
			return new Disjunction(new ImmutableArray<Expression>(simplified, Expression.class));
		else
			return this;
	}

	@Override
	public Conjunction negate() {
		return new Conjunction(negateArguments());
	}
	
	@Override
	public DNFExpression toDNF() {
		ArrayList<ConjunctiveClause> clauses = new ArrayList<>();
		for(Expression argument : arguments)
			for(ConjunctiveClause clause : argument.toDNF().arguments)
				clauses.add(clause);
		Expression expression = new DNFExpression(clauses);
		if(expression instanceof DNFExpression)
			return (DNFExpression) expression;
		else
			return expression.toDNF();
	}
}
