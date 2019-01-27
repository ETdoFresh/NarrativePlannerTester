package sabre.space;

import sabre.Action;
import sabre.Entity;
import sabre.Settings;
import sabre.State;
import sabre.logic.Expression;
import sabre.logic.Literal;
import sabre.logic.Logical;
import sabre.logic.Term;
import sabre.state.ArrayState;
import sabre.util.ArrayIterable;
import sabre.util.ImmutableArray;
import sabre.util.ImmutableList;
import sabre.util.ImmutableSet;

public class RootNode extends Node {

	private final State initial;
	
	public RootNode(State state) {
		super(null, toAction(state));
		this.initial = state;
		this.goals = new ImmutableList<Explanation>();
		for(Slot slot : state.getSearchSpace().slots)
			if(slot.property.id == Settings.INTENTIONAL_PROPERTY_ID && initial.get(slot).id == Settings.BOOLEAN_TRUE_ID)
				this.goals = this.goals.add(new Explanation(slot, this));
	}
	
	private static final ImmutableArray<Logical> NO_ARGUMENTS = new ImmutableArray<>(new Logical[0]);
	private static final ImmutableSet<Term> NO_AGENTS = new ImmutableSet<>(new Term[0]);
	
	private static final Action toAction(State state) {
		return new Action("start", "", NO_ARGUMENTS, Expression.TRUE, state.toExpression().toDNF(), NO_AGENTS);
	}
	
	public RootNode(SearchSpace space) {
		this(new ArrayState(space));
	}
	
	@Override
	public SearchSpace getSearchSpace() {
		return initial.getSearchSpace();
	}

	@Override
	public Entity get(Slot slot) {
		return initial.get(slot);
	}
	
	private static final ArrayIterable<Literal> NO_PRECONDITIONS = new ArrayIterable<>(new Literal[0]);
	
	@Override
	public Iterable<Literal> getPreconditions() {
		return NO_PRECONDITIONS;
	}
	
	@Override
	public Iterable<Literal> getEffects() {
		return Utilities.effects(event.effect, initial);
	}
}
