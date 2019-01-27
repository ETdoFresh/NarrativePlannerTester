package sabre.graph;

import sabre.Axiom;
import sabre.Settings;

public class PlanGraphAxiomNode extends PlanGraphEventNode {
	
	private static final long serialVersionUID = Settings.VERSION_UID;

	public final Axiom event;
	
	PlanGraphAxiomNode(PlanGraph graph, Axiom axiom) {
		super(graph, axiom);
		this.event = axiom;
	}

	@Override
	protected boolean setSatisfied(int level) {
		if(super.setSatisfied(level)) {
			setLevel(level);
			return true;
		}
		else
			return false;
	}
}
