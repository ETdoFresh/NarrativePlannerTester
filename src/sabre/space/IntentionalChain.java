package sabre.space;

import java.util.ArrayList;

import sabre.logic.ConjunctiveClause;
import sabre.logic.Literal;

public class IntentionalChain {

	public final Node tail;
	public final Literal link;
	public final IntentionalChain head;
	public final Explanation explanation;
	final ConjunctiveClause precondition;
	
	IntentionalChain(Node tail, Literal link, IntentionalChain head, Explanation explanation) {
		this.tail = tail;
		this.link = link;
		this.head = head;
		this.explanation = explanation;
		if(head == null)
			this.precondition = getPrecondition(tail, link, explanation);
		else
			this.precondition = getPrecondition(tail, link, head);
	}
	
	private static final ConjunctiveClause getPrecondition(Node tail, Literal link, Object head) {
		ArrayList<Literal> arguments = new ArrayList<>();
		if(head instanceof Explanation) {
			Explanation explanation = (Explanation) head;
			for(Literal argument : Utilities.preconditions(explanation.goal, explanation.satisfaction))
				arguments.add(argument);
		}
		else
			for(Literal argument : ((IntentionalChain) head).precondition.arguments)
				arguments.add(argument);
		while(arguments.contains(link))
			arguments.remove(link);
		arguments.add(link.negate());
		for(Literal argument : tail.getPreconditions())
			arguments.add(argument);
		return new ConjunctiveClause(arguments);
	}
	
	@Override
	public String toString() {
		String string = link.toString();
		if(!string.startsWith("("))
			string = "(" + string + ")";
		string = tail.event + " -" + string + "-> ";
		if(head == null)
			return string + explanation;
		else
			return string + head;
	}
	
	final IntentionalChain extend(Node tail, Literal link) {
		return Utilities.check(new IntentionalChain(tail, link, this, explanation));
	}
	
	public boolean contains(Node node) {
		if(tail == node)
			return true;
		else if(head == null)
			return false;
		else
			return head.contains(node);
	}
}
