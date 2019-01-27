package sabre.graph;

import sabre.Entity;
import sabre.Settings;
import sabre.logic.Assignment;
import sabre.logic.SlotAssignment;

public class PlanGraphAssignmentNode extends PlanGraphLiteralNode {
	
	private static final long serialVersionUID = Settings.VERSION_UID;

	public final PlanGraphSlotGroup group;
	public final SlotAssignment assignment;
	
	PlanGraphAssignmentNode(PlanGraph graph, PlanGraphSlotGroup group, Assignment assignment) {
		super(graph, assignment);
		this.group = group;
		if(assignment instanceof SlotAssignment)
			this.assignment = (SlotAssignment) assignment;
		else
			this.assignment = new SlotAssignment(graph.space.getSlot(assignment.property, assignment.arguments), (Entity) assignment.value);
		group.register(this);
	}

}
