package sabre;

public class Agent extends Entity {
	
	private static final long serialVersionUID = Settings.VERSION_UID;

	public final int index;
	
	Agent(Domain domain, int id, String name, String comment, int index) {
		super(domain, id, name, comment);
		this.index = index;
	}
	
	Agent(DomainConstructor constructor, Agent toClone) {
		super(constructor, toClone);
		this.index = toClone.index;
	}
}
