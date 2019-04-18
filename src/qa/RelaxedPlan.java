package qa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import sabre.Action;
import sabre.Event;
import sabre.graph.PlanGraphAxiomNode;
import sabre.graph.PlanGraphEventNode;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Literal;
import sabre.space.SearchSpace;
import sabre.state.MutableArrayState;

public class RelaxedPlan implements Iterable<RelaxedNode> {
	private ArrayList<RelaxedNode> nodes = new ArrayList<>();
	public ArrayList<Explanation> explanations = new ArrayList<>();
	public int clusterAssignment = -1;

	public RelaxedPlan clone() {
		RelaxedPlan clone = new RelaxedPlan();
		clone.nodes.addAll(nodes);
		clone.explanations.addAll(explanations);
		return clone;
	}

	public int getCausalDegree(RelaxedNode node, SearchSpace space) {
		int degree = 0;
		if (node.eventNode instanceof PlanGraphAxiomNode)
			return degree;
		// for each action prior to this one
		for (int i = 0; i < nodes.indexOf(node); i++)
			// degree++ for each effect that matches a precondition of this action
			for (ConjunctiveClause effect : nodes.get(i).eventNode.event.effect.toDNF().arguments)
				for (Literal e_literal : effect.arguments)
					for (ConjunctiveClause precondition : node.eventNode.event.precondition.toDNF().arguments)
						for (Literal p_literal : precondition.arguments)
							if (CheckEquals.Literal(e_literal, p_literal))
								degree++;
		// for each action after this one
		for (int i = nodes.indexOf(node); i < nodes.size(); i++)
			// degree++ for each precondition that matches an effect of this action
			for (ConjunctiveClause precondition : nodes.get(i).eventNode.event.precondition.toDNF().arguments)
				for (Literal p_literal : precondition.arguments)
					for (ConjunctiveClause effect : node.eventNode.event.effect.toDNF().arguments)
						for (Literal e_literal : effect.arguments)
							if (CheckEquals.Literal(p_literal, e_literal))
								degree++;
		// also +1 for each goal achieved by this action's effects
		for (ConjunctiveClause goal : space.goal.toDNF().arguments)
			for (Literal g_literal : goal.arguments)
				for (ConjunctiveClause effect : node.eventNode.event.effect.toDNF().arguments)
					for (Literal e_literal : effect.arguments)
						if (CheckEquals.Literal(e_literal, g_literal))
							degree++;
		return degree;
	}

	public ArrayList<RelaxedNode> getImportantSteps(SearchSpace space) {
		ArrayList<RelaxedNode> importantSteps = new ArrayList<>();
		int[] causalDegrees = new int[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
			causalDegrees[i] = getCausalDegree(nodes.get(i), space);
		int maxCausalDegree = 0;
		for (int i = 0; i < nodes.size(); i++)
			if (causalDegrees[i] > maxCausalDegree)
				maxCausalDegree = causalDegrees[i];
		for (int i = 0; i < nodes.size(); i++)
			if (causalDegrees[i] == maxCausalDegree)
				importantSteps.add(nodes.get(i));
		return importantSteps;
	}

	public boolean isValid(SearchSpace space) {
		boolean invalid = false;
		MutableArrayState state = new MutableArrayState(space);
		for (int i = 0; i < nodes.size(); i++) {
			Event event = nodes.get(i).eventNode.event;
			if (event.precondition.test(state))
				event.effect.impose(state, state); // really?
			else {
				invalid = true;
				break;
			}
		}
		return !invalid && space.goal.test(state);
	}

	public RelaxedNode last() {
		if (size() > 0)
			return nodes.get(size() - 1);
		else
			return null;
	}

	public void push(RelaxedNode action) {
		nodes.add(0, action);
	}
	
	public void pushAll(Iterable<RelaxedNode> nodes) {
		for(RelaxedNode node : nodes)
			push(node);
	}

	public RelaxedNode get(int index) {
		return nodes.get(index);
	}

	public int size() {
		return nodes.size();
	}

	public boolean contains(RelaxedNode o) {
		return nodes.contains(o);
	}

	public boolean contains(Action action) {
		for (RelaxedNode node : nodes)
			if (action.equals(node.eventNode.event))
				return true;

		return false;
	}

	public Iterator<RelaxedNode> iterator() {
		return nodes.iterator();
	}

	public float actionDistance(RelaxedPlan other) {
		return 1f - this.intersection(other) / this.union(other);
	}

	public float intersection(RelaxedPlan other) {
		if (other == null)
			return 0;
		float count = 0;
		for (RelaxedNode node : nodes) {
			if (other.contains(node))
				count++;
		}
		return count;
	}

	public float union(RelaxedPlan other) {
		if (other == null)
			return size();
		ArrayList<RelaxedNode> union = new ArrayList<>();
		union.addAll(nodes);
		for (RelaxedNode node : ((RelaxedPlan) other).nodes)
			if (!union.contains(node))
				union.add(node);
		return (float) union.size();
	}

	public RelaxedPlan unionClone(RelaxedPlan other) {
		RelaxedPlan unioned = clone();
		
		if (other == null || this == other)
			return unioned;
		
		for (RelaxedNode node : (other).nodes)
			if (!unioned.nodes.contains(node))
				unioned.nodes.add(node);
		return unioned;
	}

	public static RelaxedPlan medoid(ArrayList<RelaxedPlan> plans) {
		RelaxedPlan medoid = null;
		float[] averageDistances = new float[plans.size()];
		for (int i = 0; i < plans.size(); i++) {
			float sum = 0;
			for (RelaxedPlan other : plans) {
				sum += plans.get(i).actionDistance(other);
			}
			averageDistances[i] = sum / plans.size();
		}
		float minDistance = Float.MAX_VALUE;
		for (int i = 0; i < plans.size(); i++) {
			if (averageDistances[i] < minDistance) {
				minDistance = averageDistances[i];
				medoid = plans.get(i).clone();
			}
		}
		if (medoid == null)
			return new RelaxedPlan();
		return medoid;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof RelaxedPlan))
			return false;
		RelaxedPlan otherPlan = (RelaxedPlan) other;
		for (int i = 0; i < nodes.size(); i++) {
			if (!nodes.get(i).eventNode.event.equals(otherPlan.nodes.get(i).eventNode.event))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String str = "";
		for (RelaxedNode node : nodes)
			str += Text.BLANK + node + "\n";
		return str;
	}

	public void removeNoOps() {
		for (int i = nodes.size()-1; i >= 0; i--)
			if (nodes.get(i).toString().contains("NoOp:"))
				nodes.remove(i);
	}

	public void pushAll(HashSet<RelaxedNode> set) {
		for(RelaxedNode node : set)
			push(node);
	}
}
