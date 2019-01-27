package sabre;

import java.io.Serializable;

import sabre.search.BreadthFirst;
import sabre.search.SearchFactory;
import sabre.space.SearchSpace;
import sabre.util.Status;

public class Planner implements Serializable {

	private static final long serialVersionUID = Settings.VERSION_UID;

	private SearchSpace space = null;
	private SearchFactory factory = null;
	
	public Planner(SearchSpace space, SearchFactory factory) {
		this.space = space;
		this.factory = factory;
	}
	
	public Planner(SearchSpace space) {
		this(space, new BreadthFirst());
	}
	
	public Planner() {
		this(new SearchSpace(new Domain("empty", ""), new Status()));
	}
	
	@Override
	public String toString() {
		return "[Planner: domain \"" + getSearchSpace().domain.name + "\", search \"" + getSearchFactory().name + "\"]";
	}
	
	public SearchSpace getSearchSpace() {
		return space;
	}
	
	public void setSearchSpace(SearchSpace space) {
		this.space = space;
	}
	
	public SearchFactory getSearchFactory() {
		return factory;
	}
	
	public void setSearchFactory(SearchFactory factory) {
		this.factory = factory;
	}
}
