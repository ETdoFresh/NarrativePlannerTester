package sabre.search;

import sabre.space.Node;
import sabre.util.Status;

public class SearchWrapper extends Search {

	private final Search search;
	
	public SearchWrapper(Search search) {
		super(search.goal);
		this.search = search;
	}
	
	@Override
	public boolean isFinished() {
		return search.isFinished();
	}
	
	@Override
	public void push(Node node) {
		search.push(node);
	}
	
	@Override
	public Node peek() {
		return search.peek();
	}
	
	@Override
	public Node pop() {
		return search.pop();
	}
	
	@Override
	public boolean isSolution(Node node) {
		return search.isSolution(node);
	}

	@Override
	public long getTimeSpent() {
		return search.getTimeSpent();
	}

	@Override
	public int getNodesVisited() {
		return search.getNodesVisited();
	}

	@Override
	public int getNodesGenerated() {
		return search.getNodesGenerated();
	}

	@Override
	public int getNodesPruned() {
		return search.getNodesPruned();
	}
	
	@Override
	protected Result getNextSolution(Search root, Status status) {
		return search.getNextSolution(root, status);
	}
	
	@Override
	protected void start() {
		search.start();
	}
	
	@Override
	protected void stop() {
		search.stop();
	}
	
	@Override
	protected Node tick(Search root) {
		return search.tick(root);
	}
}
