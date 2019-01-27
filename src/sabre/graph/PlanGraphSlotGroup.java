package sabre.graph;

import java.io.Serializable;
import java.util.Arrays;

import sabre.Entity;
import sabre.Settings;
import sabre.space.Slot;

public class PlanGraphSlotGroup implements Serializable {
	
	private static final long serialVersionUID = Settings.VERSION_UID;

	public final PlanGraph graph;
	public final Slot slot;
	public final List<PlanGraphAssignmentNode> members = new List<>(PlanGraphAssignmentNode.class);
	private PlanGraphAssignmentNode[] byValue = new PlanGraphAssignmentNode[0];
	
	PlanGraphSlotGroup(PlanGraph graph, Slot slot) {
		this.graph = graph;
		this.slot = slot;
	}
	
	final void register(PlanGraphAssignmentNode node) {
		members.add(node);
		if(node.assignment.value.id >= byValue.length)
			byValue = Arrays.copyOf(byValue, node.assignment.value.id + 1);
		byValue[node.assignment.value.id] = node;
	}
	
	public PlanGraphAssignmentNode get(Entity entity) {
		if(entity.id < byValue.length)
			return byValue[entity.id];
		else
			return null;
	}
}
