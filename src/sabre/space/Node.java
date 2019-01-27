package sabre.space;

import java.util.Iterator;

import sabre.Action;
import sabre.Agent;
import sabre.Entity;
import sabre.Event;
import sabre.Plan;
import sabre.State;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Literal;
import sabre.logic.SlotAssignment;
import sabre.util.ArrayIterator;
import sabre.util.ImmutableList;

public abstract class Node implements State, Plan {
	
	static final ImmutableList<IntentionalChain> NO_EXPLANATIONS = new ImmutableList<>();
	
	public final Node parent;
	public final Event event;
	ImmutableList<Explanation> goals;
	ImmutableList<IntentionalChain> explanations = NO_EXPLANATIONS;
	
	Node(Node parent, Event event) {
		this.parent = parent;
		this.event = event;
		this.goals = parent == null ? new ImmutableList<>() : parent.goals;
	}
	
	@Override
	public String toString() {
		String string = Utilities.describePlan(this);
		string += "\nState:";
		for(Slot slot : getSearchSpace().slots)
			string += "\n  " + slot + " = " + get(slot);
		return string;
	}
	
	@Override
	public SearchSpace getSearchSpace() {
		return parent.getSearchSpace();
	}

	@Override
	public Entity get(Slot slot) {
		ConjunctiveClause clause = event.effect.toDNF().arguments.get(0);
		for(int i=clause.arguments.size()-1; i>=0; i--) {
			Literal literal = clause.arguments.get(i);
			if(literal instanceof SlotAssignment) {
				SlotAssignment assignment = (SlotAssignment) literal;
				if(assignment.slot == slot)
					return assignment.value;
			}
			else
				throw new UnsupportedOperationException("The effect \"" + literal + "\" is not recognized.");
		}
		return parent.get(slot);
	}
	
	@Override
	public int size() {
		int size = 0;
		Node current = this;
		while(current.parent != null) {
			if(current.event instanceof Action)
				size++;
			current = current.parent;
		}
		return size;
	}

	@Override
	public Iterator<Action> iterator() {
		Action[] plan = new Action[size()];
		int index = plan.length - 1;
		Node current = this;
		while(current.parent != null) {
			if(current.event instanceof Action)
				plan[index--] = (Action) current.event;
			current = current.parent;
		}
		return new ArrayIterator<Action>(plan);
	}
	
	public Iterable<Literal> getPreconditions() {
		return Utilities.preconditions(event.precondition, parent);
	}
	
	public Iterable<Literal> getEffects() {
		return Utilities.effects(event.effect, parent);
	}
	
	public Iterable<CausalLink> getCausalLinks() {
		return Utilities.causalLinks(this);
	}
		
	public Node expand(Action action) {
		return getSearchSpace().expand(this, action);
	}
	
	public boolean isExplained() {
		return true;
	}
	
	public boolean isExplained(Agent agent) {
		return true;
	}
	
	public Iterable<IntentionalChain> getExplanations() {
		return explanations;
	}
	
	final void checkGoals() {
		ImmutableList<Explanation> current = goals;
		while(current.size != 0) {
			if(current.first.goal.test(this) && !current.first.goal.test(parent))
				makeIntentionalChains(new Explanation(current.first, this));
			current = current.rest;
		}
	}
	
	private final void makeIntentionalChains(Explanation explanation) {
		for(CausalLink link : Utilities.causalLinks(explanation)) {
			IntentionalChain chain = explanation.extend(link.tail, link.link);
			if(chain != null)
				makeIntentionalChains(chain);
		}
	}
	
	private final void makeIntentionalChains(IntentionalChain chain) {
		boolean propagate = true;
		for(CausalLink link : chain.tail.getCausalLinks()) {
			IntentionalChain extended = chain.extend(link.tail, link.link);
			if(extended != null) {
				makeIntentionalChains(extended);
				propagate = false;
			}
		}
		if(propagate)
			propagate(chain);
	}
	
	void propagate(IntentionalChain chain) {
		if(chain.contains(this))
			explanations = explanations.add(find(chain, this));
		if(parent != null)
			parent.propagate(chain);
	}
	
	private static final IntentionalChain find(IntentionalChain chain, Node node) {
		if(chain == null)
			return null;
		else if(chain.tail == node)
			return chain;
		else
			return find(chain.head, node);
	}
}
