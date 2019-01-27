package sabre.space;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import sabre.Action;
import sabre.Axiom;
import sabre.Domain;
import sabre.Event;
import sabre.Property;
import sabre.Settings;
import sabre.graph.MutablePlanGraph;
import sabre.graph.PlanGraph;
import sabre.logic.Expression;
import sabre.logic.Literal;
import sabre.logic.Logical;
import sabre.logic.SlotAssignment;
import sabre.state.MutableListState;
import sabre.util.ImmutableSet;
import sabre.util.Status;

public class SearchSpace implements Serializable {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public final Domain domain;
	public final ImmutableSet<Slot> slots;
	final SlotTree tree = new SlotTree();
	public final ImmutableSet<Action> actions;
	public final ImmutableSet<Axiom> axioms;
	public final Expression goal;
	public final PlanGraph graph;

	public SearchSpace(Domain domain, Status status) {
		this.domain = domain;
		// Propositionalize domain.
		Propositionalizer propositionalizer = new Propositionalizer(this, status);
		status.setFormat("Simplifying search space: calculating initial state...");
		// Apply axioms to calculate initial state.
		MutableListState initial = new MutableListState(this);
		for(Expression expression : domain.initial)
			initial.impose(expression);
		boolean loop = true;
		while(loop) {
			loop = false;
			for(Event event : propositionalizer.events)
				if(event instanceof Axiom)
					if(event.precondition.test(initial))
						loop = initial.impose(event.effect) || loop;
		}
		// Remake slots with correct initial values.
		Slot[] slots = tree.toArray();
		tree.clear();
		for(int i=0; i<slots.length; i++) {
			Slot slot = slots[i];
			slot = new Slot(this, slot.id, slot.property, slot.arguments, initial.get(slot));
			slots[i] = slot;
			tree.put(slot);
			tree.put(new SlotAssignment(slot, slot.initial));
		}
		// For any expressions inside slots, replace assignments with slot assignments.
		SlotSet slotSet = new SlotSet(tree, status);
		// Eliminate impossible events and calculate all possible values for each slot.
		status.setFormat("Simplifying search space: ", 0, " events possible");
		ArrayList<Event> possible = new ArrayList<>();
		NonDeletingState state = new NonDeletingState(this);
		loop = true;
		while(loop) {
			loop = false;
			for(Iterator<Event> iterator = propositionalizer.events.iterator(); iterator.hasNext();) {
				Event event = iterator.next();
				if(state.canBeTrue(event.precondition)) {
					iterator.remove();
					possible.add(event);
					status.update(1, possible.size());
					state.impose(event.effect);
					loop = true;
				}
			}
		}
		// For every event and the goal, replace assignments with slot assignments.
		status.setFormat("Simplifying search space: simplifying events");
		Event[] events = possible.toArray(new Event[possible.size()]);
		for(int i=0; i<events.length; i++)
			events[i] = events[i].substitute(slotSet);
		Expression goal;
		if(state.canBeTrue(domain.goal))
			goal = (Expression) domain.goal.substitute(slotSet);
		else
			goal = Expression.FALSE;
		// Remove immutable slots and simplify them
		slotSet.simplify(status);
		// Sort events and simplify them.
		status.setFormat("Simplifying search space: simplifying ", 0, " events");
		TreeSet<Action> actions = new TreeSet<>();
		TreeSet<Axiom> axioms = new TreeSet<>();
		for(Event event : events) {
			event = event.substitute(slotSet);
			Expression precondition = event.precondition.simplify().toDNF();
			Expression effect = event.effect.simplify().toDNF();
			if(event instanceof Action)
				actions.add(new Action(event.name, event.comment, event.arguments, precondition, effect, ((Action) event).agents));
			else
				axioms.add(new Axiom(event.name, event.comment, event.arguments, precondition, effect));
			status.update(1, actions.size() + axioms.size());
		}
		// Simplify goal.
		goal = (Expression) goal.substitute(slotSet);
		goal = goal.simplify().toDNF();
		// Finalize search space.
		status.setFormat("Simplifying search space: building plan graph");
		this.slots = new ImmutableSet<>(slotSet.slots);
		this.tree.clear();
		for(Slot slot : this.slots)
			this.tree.put(slot);
		this.actions = new ImmutableSet<>(actions, Action.class);
		this.axioms = new ImmutableSet<>(axioms, Axiom.class);
		MutablePlanGraph graph = new MutablePlanGraph(this);
		for(Slot slot : this.slots)
			graph.addLiteral((Literal) new SlotAssignment(slot, slot.initial).substitute(slotSet));
		for(Action action : actions)
			graph.addEvent(action);
		for(Axiom axiom : axioms)
			graph.addEvent(axiom);
		this.goal = goal;
		this.graph = graph;
	}
	
	@Override
	public String toString() {
		return "[Search Space \"" + domain.name + "\": " + slots.size() + " slots, " + actions.size() + " actions, " + axioms.size() + " axioms]";
	}
	
	public Slot getSlot(Property property, Iterable<? extends Logical> arguments) {
		Slot slot = tree.get(property, arguments);
		if(slot == null)
			throw new IllegalArgumentException("Slot \"" + sabre.Utilities.toFunctionString(property.name, arguments) + "\" not defined.");
		else
			return slot;
	}
	
	private final MutableNodeState mutable = new MutableNodeState();	
	
	final Node expand(Node parent, Action action) {
		mutable.before = parent;
		mutable.event = action;
		mutable.after = new ActionNode(parent, action);
		action.effect.impose(mutable.before, mutable);
		mutable.before = mutable.after;
		boolean loop = true;
		while(loop) {
			loop = false;
			graph.initialize(mutable.before);
			for(int i=0; i<graph.events.size(); i++) {
				mutable.event = graph.events.get(i).event;
				mutable.after = null;
				if(mutable.event.precondition.test(mutable.before) && mutable.event.effect.impose(mutable.before, mutable)) {
					mutable.before = mutable.after;
					loop = true;
				}
			}
		}
		return mutable.before;
	}
}
