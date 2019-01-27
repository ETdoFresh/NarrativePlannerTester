package sabre.search;

import java.util.ArrayDeque;

import sabre.Settings;
import sabre.logic.Expression;
import sabre.space.Node;

public class BreadthFirst extends SearchFactory {

	private static final long serialVersionUID = Settings.VERSION_UID;

	public BreadthFirst() {
		super("breadth");
	}

	@Override
	public Search makeSearch(Expression goal) {
		return new BreadthFirstSearch(goal);
	}
	
	private class BreadthFirstSearch extends Search {
		
		protected final ArrayDeque<Node> queue = new ArrayDeque<>();
		
		public BreadthFirstSearch(Expression goal) {
			super(goal);
		}

		@Override
		public boolean isFinished() {
			return queue.isEmpty();
		}
		
		@Override
		public void push(Node node) {
			queue.add(node);
		}
		
		@Override
		public Node peek() {
			return queue.peek();
		}

		@Override
		public Node pop() {
			return queue.pop();
		}
	}
}
