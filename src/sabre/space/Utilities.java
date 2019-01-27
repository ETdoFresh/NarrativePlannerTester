package sabre.space;

import java.util.ArrayList;

import sabre.Agent;
import sabre.State;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Expression;
import sabre.logic.Literal;
import sabre.logic.Term;

final class Utilities {
	
	static final Iterable<Literal> preconditions(Expression expression, State state) {
		ArrayList<Literal> preconditions = new ArrayList<>();
		for(ConjunctiveClause clause : expression.toDNF().arguments)
			if(clause.test(state))
				for(Literal literal : clause.arguments)
					if(literal != Expression.TRUE)
						preconditions.add(literal);
		return preconditions;
	}
	
	static final Iterable<Literal> effects(Expression expression, State state) {
		ArrayList<Literal> effects = new ArrayList<>();
		for(Literal literal : expression.toDNF().arguments.get(0).arguments)
			if(literal != Expression.TRUE)
				effects.add(literal);
		return effects;
	}
	
	private static final Iterable<CausalLink> causalLinks(Expression expression, Node state, Node head) {
		ArrayList<CausalLink> links = new ArrayList<>();
		for(Literal precondition : preconditions(expression, state)) {
			Node tail = state;
			while(tail != null && precondition.test(tail)) {
				for(Literal effect : tail.getEffects())
					if(precondition.matches(effect))
						links.add(new CausalLink(tail, precondition, head));
				tail = tail.parent;
			}
		}
		return links;
	}
	
	static final Iterable<CausalLink> causalLinks(Node node) {
		if(node.parent == null)
			return causalLinks(node.event.precondition, node, node);
		else
			return causalLinks(node.event.precondition, node.parent, node);
	}
	
	static final Iterable<CausalLink> causalLinks(Explanation explanation) {
		return causalLinks(explanation.goal, explanation.satisfaction, null);
	}
	
	static final IntentionalChain check(IntentionalChain chain) {
		Node parent = chain.tail.parent == null ? chain.tail : chain.tail.parent;
		if(comesBefore(chain.explanation.motivation, chain.tail) && chain.precondition.test(parent))
			return chain;
		else
			return null;
	}
	
	private static final boolean comesBefore(Node before, Node after) {
		while(after != null && after != before)
			after = after.parent;
		return after == before;
	}
	
	static final String describePlan(Node node) {
		String string = "";
		while(node.parent != null) {
			if(node instanceof ActionNode)
				string = "\n  " + describeAction((ActionNode) node) + string;
			node = node.parent;
		}
		return "Plan:" + string;
	}
	
	private static final String describeAction(ActionNode node) {
		String string = node.event.toString();
		for(Term agent : node.event.agents)
			if(!node.isExplained((Agent) agent))
				string += " [not explained for " + agent + "]";
		return string;
	}
}
