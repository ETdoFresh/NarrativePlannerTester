package qa;

import java.util.ArrayList;
import sabre.graph.PlanGraphEventNode;

public class RelaxedNode {
	public PlanGraphEventNode eventNode;
	public ArrayList<Explanation> explanations;
		
	public RelaxedNode(PlanGraphEventNode eventNode, ArrayList<Explanation> explanations) {
		super();
		this.eventNode = eventNode;
		this.explanations = explanations;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RelaxedNode) {
			RelaxedNode other = (RelaxedNode)obj;
			return eventNode.equals(other.eventNode) && explanations.equals(other.explanations);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return eventNode.toString();
	}
}
