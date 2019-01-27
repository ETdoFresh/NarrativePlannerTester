package sabre.graph;

import sabre.Event;
import sabre.Settings;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Literal;
import sabre.util.ImmutableSet;
import sabre.util.MutableSet;

public class PlanGraphEventNode extends PlanGraphSatisfiableNode {
	
	private static final long serialVersionUID = Settings.VERSION_UID;

	public final Event event;
	public final ImmutableSet<PlanGraphNode> children = new MutableSet<>(new PlanGraphNode[0]);
	
	PlanGraphEventNode(PlanGraph graph, Event event) {
		super(graph, event.precondition.toDNF());
		this.event = event;
		graph.eventMap.put(event, this);
		for(ConjunctiveClause clause : event.effect.toDNF().arguments) {
			for(Literal literal : clause.arguments) {
				PlanGraphLiteralNode child = graph.addLiteral(literal);
				((MutableSet<PlanGraphNode>) child.parents).add(this);
				((MutableSet<PlanGraphNode>) children).add(child);
			}
		}
	}
	
	@Override
	public String toString() {
		return "[" + event.toString() + "; level=" + getLevel() + "]";
	}
	
	@Override
	public boolean setLevel(int level) {
		if(super.setLevel(level)) {
			graph.events.add(this);
			for(int i=0; i<children.size(); i++)
				children.get(i).setLevel(level);
			return true;
		}
		else
			return false;
	}
}
