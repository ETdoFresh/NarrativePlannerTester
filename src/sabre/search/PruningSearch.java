package sabre.search;

import sabre.space.Node;

public abstract class PruningSearch extends SearchWrapper {

	private int pruned = 0;
	
	public PruningSearch(Search search) {
		super(search);
	}

	@Override
	public void push(Node node) {
		if(prune(node))
			pruned++;
		else
			super.push(node);
	}
	
	@Override
	public int getNodesPruned() {
		return pruned + super.getNodesPruned();
	}
	
	protected abstract boolean prune(Node node);
}
