package sabre.space;

import java.util.LinkedHashSet;

import sabre.Entity;
import sabre.Event;
import sabre.logic.Expression;
import sabre.logic.HashSubstitution;
import sabre.logic.QuantifierRemover;
import sabre.logic.TermVariable;
import sabre.util.Status;

final class Propositionalizer {

	public final SearchSpace space;
	public final LinkedHashSet<Event> events = new LinkedHashSet<>();
	private final Status status;
	private final NonDeletingState state;
	
	public Propositionalizer(SearchSpace space, Status status) {
		this.space = space;
		this.status = status;
		this.status.setFormat("Propositionalizing domain: ", 0, " slots, ", 0, " events");
		this.state = new NonDeletingState(space);
		for(Expression initial : space.domain.initial)
			this.state.impose((Expression) initial.substitute(new QuantifierRemover(space.domain)));
		this.state.canBeTrue(space.domain.goal);
		int oldSize = -1;
		int newSize = space.tree.size() + events.size();
		while(newSize > oldSize) {
			oldSize = newSize;
			for(Event action : space.domain.actions)
				propositionalize(action);
			for(Event axiom : space.domain.axioms)
				propositionalize(axiom);
			newSize = space.tree.size() + events.size();
		}
	}
	
	private void propositionalize(Event event) {
		Expression precondition = (Expression) event.precondition.substitute(new QuantifierRemover(space.domain));
		propositionalize(event, precondition, new HashSubstitution(), 0);
	}
	
	private void propositionalize(Event event, Expression precondition, HashSubstitution substitution, int index) {
		status.update(1, space.tree.size());
		status.update(3, events.size());
		if(!state.canBeTrue(precondition))
			return;
		else if(index == event.arguments.size()) {
			event = event.substitute(new QuantifierRemover(space.domain, substitution));
			events.add(event);
			state.impose(event.effect);
		}
		else if(event.arguments.get(index) instanceof TermVariable) {
			TermVariable parameter = (TermVariable) event.arguments.get(index);
			for(Entity value : space.domain.getEntities(parameter.type)) {
				substitution.set(parameter, value);
				propositionalize(event, (Expression) precondition.substitute(substitution), substitution, index + 1);
			}
			substitution.set(parameter, parameter);
		}
		else
			propositionalize(event, precondition, substitution, index + 1);
	}
}
