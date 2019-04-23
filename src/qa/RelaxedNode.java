package qa;

import java.io.Serializable;
import java.util.ArrayList;
import sabre.graph.PlanGraphEventNode;

public class RelaxedNode implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public PlanGraphEventNode eventNode;
	public ArrayList<Explanation> explanations = new ArrayList<>();
	public int level;
		
	public RelaxedNode(PlanGraphEventNode eventNode, ArrayList<Explanation> explanations, int level) {
		super();
		this.eventNode = eventNode;
		this.explanations = explanations;
		this.level = level;
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof RelaxedNode) {
			RelaxedNode otherNode = (RelaxedNode)other;
			if(explanations==null && otherNode.explanations==null)
				return eventNode.equals(otherNode.eventNode) && level == otherNode.level;
			else if(explanations != null && otherNode.explanations != null)
				return eventNode.equals(otherNode.eventNode) 
					&& level == otherNode.level
					&& explanations.equals(otherNode.explanations);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "[" + eventNode.event + " level=" + level + "]";
	}
}
