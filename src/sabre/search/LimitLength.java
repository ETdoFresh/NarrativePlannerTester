package sabre.search;

import sabre.Settings;
import sabre.logic.Expression;
import sabre.space.Node;
import sabre.util.ArgumentQueue;

public class LimitLength extends SearchFactory {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	private final SearchFactory previous;
	private final int maxLength;
	
	public LimitLength(SearchFactory previous, int maxLength) {
		super(previous.name + " maxlength " + maxLength);
		this.previous = previous;
		this.maxLength = maxLength;
	}
	
	public LimitLength(SearchFactory previous, ArgumentQueue arguments) {
		this(previous, Integer.parseInt(arguments.pop()));
	}
	
	@Override
	public Search makeSearch(Expression goal) {
		return new LimitLengthSearch(previous.makeSearch(goal));
	}
	
	private class LimitLengthSearch extends PruningSearch {

		public LimitLengthSearch(Search search) {
			super(search);
		}

		@Override
		protected boolean prune(Node node) {
			return node.size() > maxLength;
		}
	}
}
