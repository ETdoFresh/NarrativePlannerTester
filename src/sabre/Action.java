package sabre;

import java.util.function.Function;

import sabre.logic.Expression;
import sabre.logic.Logical;
import sabre.logic.Term;
import sabre.util.ImmutableArray;
import sabre.util.ImmutableSet;
import sabre.util.MutableSet;

public class Action extends Event {
	
	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public final ImmutableSet<Term> agents;

	public Action(String name, String comment, ImmutableArray<Logical> arguments, Expression precondition, Expression effect, ImmutableSet<Term> agents) {
		super(name, comment, arguments, precondition, effect);
		checkAgents(agents);
		this.agents = agents;
	}
	
	protected Action(Action toClone, Function<Object, Object> substitution) {
		super(toClone, substitution);
		this.agents = new MutableSet<>(new Term[0]);
		for(Term agent : toClone.agents)
			((MutableSet<Term>) agents).add((Term) substitution.apply(agent));
		checkAgents(agents);
	}
	
	private static final void checkAgents(ImmutableSet<Term> agents) {
		for(Term term : agents)
			if(!Utilities.isAgent(term))
				throw new FormatException("\"" + term + "\" is not an agent.");
	}
	
	public Action substitute(Function<Object, Object> substitution) {
		return new Action(this, substitution);
	}
}
