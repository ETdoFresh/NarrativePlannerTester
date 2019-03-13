package sabre.search;

import sabre.Action;
import sabre.graph.PlanGraph;
import sabre.logic.Expression;
import sabre.space.Node;
import sabre.util.Status;

public abstract class Search {

	public final Expression goal;
	private long start = -1;
	private long duration = 0;
	private int visited = 0;
	private int generated = 0;
	
	public Search(Expression goal) {
		this.goal = goal;
	}
	
	public abstract boolean isFinished();
	
	public abstract void push(Node node);
	
	public abstract Node peek();
	
	public abstract Node pop();
	
	public boolean isSolution(Node node) {
		if(!goal.test(node))
			return false;
		while(node != null) {
			if(!node.isExplained())
				return false;
			node = node.parent;
		}
		return true;
	}

	public long getTimeSpent() {
		return duration + (start == -1 ? 0 : System.currentTimeMillis() - start);
	}

	public int getNodesVisited() {
		return visited;
	}

	public int getNodesGenerated() {
		return generated;
	}

	public int getNodesPruned() {
		return 0;
	}
	
	public final Result getNextSolution() {
		return getNextSolution(new Status());
	}
	
	public final Result getNextSolution(Status status) {
		return getNextSolution(this, status);
	}
	
	protected Result getNextSolution(Search root, Status status) {
		status.setFormat("Searching...  ", 0, " visited; ", 0, " generated; ", 0, " pruned"); 
		root.start();
		Node plan = null;
		while(plan == null && !root.isFinished()) {
			plan = root.tick(root);
			status.update(1, root.getNodesVisited());
			status.update(3, root.getNodesGenerated());
			status.update(5, root.getNodesPruned());
			
			if (Thread.interrupted()) return null;
		}
		root.stop();
		return new Result(plan, root.getTimeSpent(), root.getNodesVisited(), root.getNodesGenerated(), root.getNodesPruned());
	}
	
	protected void start() {
		start = System.currentTimeMillis();
	}
	
	protected void stop() {
		duration += System.currentTimeMillis() - start;
		start = -1;
	}
	
	protected Node tick(Search root) {
		Node current = root.pop();
		visited++;
		PlanGraph graph = current.getSearchSpace().graph;
		graph.initialize(current);
		expand(root, graph, current, graph.pending.size() - 1);
		if(root.isSolution(current))
			return current;
		else
			return null;
	}
	
	private final void expand(Search root, PlanGraph graph, Node parent, int index) {
		if(index != -1) {
			Action action = graph.pending.get(index).event;
			expand(root, graph, parent, index - 1);
			root.push(parent.expand(action));
			generated++;
		}
	}
}
