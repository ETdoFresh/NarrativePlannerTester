package sabre.graph;

import sabre.Settings;

public abstract class PlanGraphNode extends Node {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public final PlanGraph graph;
	private int level = -1;
	
	PlanGraphNode(PlanGraph graph) {
		super(graph);
		this.graph = graph;
	}
	
	@Override
	protected void reset() {
		super.reset();
		level = -1;
	}
	
	public boolean exists(int level) {
		return exists() && this.level <= level;
	}
	
	public int getLevel() {
		if(exists())
			return level;
		else
			return -1;
	}
	
	public boolean setLevel(int level) {
		if(activate()) {
			this.level = level;
			return true;
		}
		else
			return false;
	}
}
