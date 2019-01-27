package sabre.state;

import java.io.Serializable;

import sabre.Action;
import sabre.Axiom;
import sabre.Entity;
import sabre.Settings;
import sabre.State;
import sabre.graph.PlanGraph;
import sabre.logic.Expression;
import sabre.space.SearchSpace;
import sabre.space.Slot;

public class ListState implements State, Serializable {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	protected final State previous;
	
	public ListState(State previous) {
		this.previous = previous;
	}
	
	public ListState(SearchSpace space) {
		MutableListState mutable = new MutableListState(new ListStateRoot(space));
		for(Expression expression : space.domain.initial)
			expression.impose(mutable.current, mutable);
		this.previous = mutable.current;
	}
	
	public ListState(State previous, Expression expression) {
		MutableListState mutable = new MutableListState(previous);
		expression.impose(previous, mutable);
		this.previous = mutable.current;
	}
	
	public ListState(State previous, Action action) {
		MutableListState mutable = new MutableListState(previous);
		action.effect.impose(previous, mutable);
		PlanGraph graph = previous.getSearchSpace().graph;
		boolean loop = true;
		while(loop) {
			loop = false;
			graph.initialize(mutable);
			for(int i=0; i<graph.events.size(); i++) {
				Axiom axiom = (Axiom) graph.events.get(i).event;
				if(axiom.precondition.test(mutable) && axiom.effect.impose(mutable.current, mutable))
					loop = true;
			}
		}
		this.previous = mutable.current;
	}
	
	@Override
	public boolean equals(Object other) {
		return State.equals(this, other);
	}
	
	@Override
	public int hashCode() {
		return State.hashCode(this);
	}
	
	@Override
	public String toString() {
		return State.toString(this);
	}
	
	@Override
	public SearchSpace getSearchSpace() {
		return previous.getSearchSpace();
	}

	@Override
	public Entity get(Slot slot) {
		return previous.get(slot);
	}
	
	/*
	public ListState impose(Expression expression) {
		MutableListState mutable = new MutableListState(this);
		expression.impose(this, mutable);
		return (ListState) mutable.current;
	}
	
	public ListState impose(Action action) {
		MutableListState mutable = new MutableListState(this);
		action.effect.impose(this, mutable);
		PlanGraph graph = getSearchSpace().graph;
		boolean loop = true;
		while(loop) {
			loop = false;
			graph.initialize(mutable);
			for(int i=0; i<graph.events.size(); i++) {
				Axiom axiom = (Axiom) graph.events.get(i).event;
				if(axiom.precondition.test(mutable) && axiom.effect.impose(mutable.current, mutable))
					loop = true;
			}
		}
		return (ListState) mutable.current;
	}
	*/
}
