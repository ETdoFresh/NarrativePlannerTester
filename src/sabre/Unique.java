package sabre;

import java.io.Serializable;

public abstract class Unique implements Serializable {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public final Domain domain;
	public final int id;
	public final String name;
	public final String comment;
	
	public Unique(Domain domain, int id, String name, String comment) {
		this.domain = domain;
		this.id = id;
		this.name = name;
		this.comment = comment == null ? "" : comment;
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
