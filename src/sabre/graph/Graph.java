package sabre.graph;

import java.io.Serializable;

import sabre.Settings;

public abstract class Graph implements Serializable {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	protected final List<Node> nodes = new List<>(Node.class);
	int key = 0;
	
	protected void initialize() {
		if(key == Integer.MAX_VALUE) {
			for(int i=0; i<nodes.size(); i++)
				nodes.get(i).reset();
			key = -1;
		}
		key++;
	}
}
