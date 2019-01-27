package sabre.graph;

import java.io.Serializable;

import sabre.Settings;

public abstract class Node implements Serializable {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public final Graph graph;
	private int key = -1;
	
	Node(Graph graph) {
		this.graph = graph;
		graph.nodes.add(this);
		reset();
	}
	
	protected void reset() {
		key = -1;
	}
	
	public boolean exists() {
		return key == graph.key;
	}
	
	protected boolean activate() {
		if(key == graph.key)
			return false;
		else {
			key = graph.key;
			return true;
		}
	}
}
