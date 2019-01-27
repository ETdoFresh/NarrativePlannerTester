package sabre.graph;

import sabre.Event;
import sabre.Settings;
import sabre.logic.Literal;
import sabre.space.SearchSpace;

public class MutablePlanGraph extends PlanGraph {
	
	private static final long serialVersionUID = Settings.VERSION_UID;

	public MutablePlanGraph(SearchSpace space) {
		super(space);
	}

	@Override
	public PlanGraphLiteralNode addLiteral(Literal literal) {
		return super.addLiteral(literal);
	}
	
	@Override
	public PlanGraphEventNode addEvent(Event event) {
		return super.addEvent(event);
	}
}
