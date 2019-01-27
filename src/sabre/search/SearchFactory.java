package sabre.search;

import java.io.Serializable;

import sabre.Settings;
import sabre.logic.Expression;

public abstract class SearchFactory implements Serializable {

	private static final long serialVersionUID = Settings.VERSION_UID;

	public final String name;
	
	public SearchFactory(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "[Search Factory: " + name + "]";
	}
	
	public abstract Search makeSearch(Expression goal);
}
