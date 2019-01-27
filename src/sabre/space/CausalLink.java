package sabre.space;

import sabre.logic.Literal;

public class CausalLink {

	public final Node tail;
	public final Literal link;
	public final Node head;
	
	public CausalLink(Node tail, Literal link, Node head) {
		this.tail = tail;
		this.link = link;
		this.head = head;
	}
}
