package sabre.graph;

import java.io.StringWriter;
import java.util.HashMap;

import sabre.Action;
import sabre.Axiom;
import sabre.Event;
import sabre.Settings;
import sabre.State;
import sabre.logic.Assignment;
import sabre.logic.Literal;
import sabre.space.SearchSpace;
import sabre.space.Slot;

public class PlanGraph extends Graph {
	
	private static final long serialVersionUID = Settings.VERSION_UID;

	public final SearchSpace space;
	public final List<PlanGraphLiteralNode> literals = new List<>(PlanGraphLiteralNode.class);
	public final List<PlanGraphEventNode> events = new List<>(PlanGraphEventNode.class);
	public final List<PlanGraphActionNode> pending = new List<>(PlanGraphActionNode.class);
	private int levels = 0;
	
	public PlanGraph(SearchSpace space) {
		this.space = space;
	}
	
	@Override
	public String toString() {
		StringWriter string = new StringWriter();
		string.append("Plan Graph for \"" + space.domain.name + "\":");
		for(int level=0; level<levels; level++) {
			string.append("\n  Level " + level + "\n    Literals:");
			for(PlanGraphLiteralNode literal : literals)
				if(literal.getLevel() == level)
					string.append("\n      " + literal.literal);
			string.append("\n    Events:");
			for(PlanGraphEventNode event : events)
				if(event.getLevel() == level)
					string.append("\n      " + event.event);
		}
		return string.toString();
	}
	
	//-------------------------------------------------------------------------
	// Node Construction
	//-------------------------------------------------------------------------
	
	final HashMap<Literal, PlanGraphLiteralNode> literalMap = new HashMap<>();
	private final List<PlanGraphSlotGroup> groups = new List<>(PlanGraphSlotGroup.class);
	private final List<PlanGraphLiteralNode> nonAssignments = new List<>(PlanGraphLiteralNode.class);
	
	public PlanGraphLiteralNode getLiteral(Literal literal) {
		return literalMap.get(literal);
	}
	
	protected PlanGraphLiteralNode addLiteral(Literal literal) {
		PlanGraphLiteralNode node = getLiteral(literal);
		if(node == null) {
			if(literal instanceof Assignment) {
				Assignment assignment = (Assignment) literal;
				Slot slot = space.getSlot(assignment.property, assignment.arguments);
				PlanGraphSlotGroup group;
				if(slot.id < groups.size())
					group = groups.get(slot.id);
				else {
					group = new PlanGraphSlotGroup(this, slot);
					groups.put(slot.id, group);
				}
				node = new PlanGraphAssignmentNode(this, group, assignment);
			}
			else {
				node = new PlanGraphLiteralNode(this, literal);
				nonAssignments.add(node);
			}
		}
		return node;
	}
	
	final HashMap<Event, PlanGraphEventNode> eventMap = new HashMap<>();
	
	public PlanGraphEventNode getEvent(Event event) {
		return eventMap.get(event);
	}
	
	protected PlanGraphEventNode addEvent(Event event) {
		PlanGraphEventNode node = getEvent(event);
		if(node == null) {
			if(event instanceof Action)
				node = new PlanGraphActionNode(this, (Action) event);
			else
				node = new PlanGraphAxiomNode(this, (Axiom) event);
		}
		return node;
	}
	
	//-------------------------------------------------------------------------
	// Graph Extension
	//-------------------------------------------------------------------------
	
	public int size() {
		return levels;
	}
	
	public void initialize(State state) {
		initialize();
		literals.clear();
		events.clear();
		pending.clear();
		levels = 1;
		for(int i=0; i<groups.size(); i++) {
			PlanGraphSlotGroup group = groups.get(i);
			if(group != null)
				group.get(state.get(group.slot)).setLevel(0);
		}
		for(int i=0; i<nonAssignments.size(); i++) {
			PlanGraphLiteralNode node = nonAssignments.get(i);
			if(node.literal.test(state))
				node.setLevel(0);
		}
	}
	
	public boolean hasLeveledOff() {
		return pending.size() == 0;
	}
	
	public boolean extend() {
		if(hasLeveledOff())
			return true;
		setLevel(pending.size() - 1, levels++);
		return hasLeveledOff();
	}
	
	private final void setLevel(int index, int level) {
		if(index == -1)
			pending.clear();
		else {
			PlanGraphActionNode action = pending.get(index);
			setLevel(index - 1, level);
			action.setLevel(level);
		}
	}
}
