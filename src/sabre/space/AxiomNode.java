package sabre.space;

import sabre.Axiom;

public class AxiomNode extends Node {

	public final Axiom event;
	
	AxiomNode(Node parent, Axiom axiom) {
		super(parent, axiom);
		this.event = axiom;
		checkGoals();
	}
}
