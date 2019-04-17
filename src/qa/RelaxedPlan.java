package qa;

import java.util.ArrayList;
import java.util.Iterator;

import sabre.Action;
import sabre.Event;
import sabre.graph.PlanGraphAxiomNode;
import sabre.graph.PlanGraphEventNode;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Literal;
import sabre.space.SearchSpace;
import sabre.state.MutableArrayState;

public class RelaxedPlan implements Iterable<PlanGraphEventNode> {
	private ArrayList<PlanGraphEventNode> actions = new ArrayList<>();
	public ArrayList<Explanation> explanations = new ArrayList<>();
	public int clusterAssignment = -1;

	public RelaxedPlan clone() {
		RelaxedPlan clone = new RelaxedPlan();
		clone.actions.addAll(actions);
		clone.explanations.addAll(explanations);
		return clone;
	}

	public int getCausalDegree(PlanGraphEventNode action, SearchSpace space) {
		int degree = 0;
		if (action instanceof PlanGraphAxiomNode)
			return degree;
		// for each action prior to this one
		for (int i = 0; i < actions.indexOf(action); i++)
			// degree++ for each effect that matches a precondition of this action
			for (ConjunctiveClause effect : actions.get(i).event.effect.toDNF().arguments)
				for (Literal e_literal : effect.arguments)
					for (ConjunctiveClause precondition : action.event.precondition.toDNF().arguments)
						for (Literal p_literal : precondition.arguments)
							if (CheckEquals.Literal(e_literal, p_literal))
								degree++;
		// for each action after this one
		for (int i = actions.indexOf(action); i < actions.size(); i++)
			// degree++ for each precondition that matches an effect of this action
			for (ConjunctiveClause precondition : actions.get(i).event.precondition.toDNF().arguments)
				for (Literal p_literal : precondition.arguments)
					for (ConjunctiveClause effect : action.event.effect.toDNF().arguments)
						for (Literal e_literal : effect.arguments)
							if (CheckEquals.Literal(p_literal, e_literal))
								degree++;
		// also +1 for each goal achieved by this action's effects
		for (ConjunctiveClause goal : space.goal.toDNF().arguments)
			for (Literal g_literal : goal.arguments)
				for (ConjunctiveClause effect : action.event.effect.toDNF().arguments)
					for (Literal e_literal : effect.arguments)
						if (CheckEquals.Literal(e_literal, g_literal))
							degree++;
		return degree;
	}

	public ArrayList<PlanGraphEventNode> getImportantSteps(SearchSpace space) {
		ArrayList<PlanGraphEventNode> importantSteps = new ArrayList<>();
		int[] causalDegrees = new int[actions.size()];
		for (int i = 0; i < actions.size(); i++)
			causalDegrees[i] = getCausalDegree(actions.get(i), space);
		int maxCausalDegree = 0;
		for (int i = 0; i < actions.size(); i++)
			if (causalDegrees[i] > maxCausalDegree)
				maxCausalDegree = causalDegrees[i];
		for (int i = 0; i < actions.size(); i++)
			if (causalDegrees[i] == maxCausalDegree)
				importantSteps.add(actions.get(i));
		return importantSteps;
	}

	public boolean isValid(SearchSpace space) {
		boolean invalid = false;
		MutableArrayState state = new MutableArrayState(space);
		for (int i = 0; i < actions.size(); i++) {
			Event event = actions.get(i).event;
			if (event.precondition.test(state))
				event.effect.impose(state, state); // really?
			else {
				invalid = true;
				break;
			}
		}
		return !invalid && space.goal.test(state);
	}

	public PlanGraphEventNode last() {
		if (size() > 0)
			return actions.get(size() - 1);
		else
			return null;
	}

	public void push(PlanGraphEventNode action) {
		actions.add(0, action);
	}
	
	public void pushAll(Iterable<PlanGraphEventNode> actions) {
		for(PlanGraphEventNode action : actions)
			push(action);
	}

	public PlanGraphEventNode get(int index) {
		return actions.get(index);
	}

	public int size() {
		return actions.size();
	}

	public boolean contains(PlanGraphEventNode o) {
		return actions.contains(o);
	}

	public boolean contains(Action action) {
		for (PlanGraphEventNode actionNode : actions)
			if (action.equals(actionNode.event))
				return true;

		return false;
	}

	public Iterator<PlanGraphEventNode> iterator() {
		return actions.iterator();
	}

	public float actionDistance(RelaxedPlan other) {
		return 1f - this.intersection(other) / this.union(other);
	}

	public float intersection(RelaxedPlan other) {
		if (other == null)
			return 0;
		float count = 0;
		for (PlanGraphEventNode action : actions) {
			if (other.contains(action))
				count++;
		}
		return count;
	}

	public float union(RelaxedPlan other) {
		if (other == null)
			return size();
		ArrayList<PlanGraphEventNode> union = new ArrayList<>();
		union.addAll(actions);
		for (PlanGraphEventNode action : ((RelaxedPlan) other).actions)
			if (!union.contains(action))
				union.add(action);
		return (float) union.size();
	}

	public RelaxedPlan unionClone(RelaxedPlan other) {
		RelaxedPlan unioned = clone();
		
		if (other == null || this == other)
			return unioned;
		
		for (PlanGraphEventNode action : (other).actions)
			if (!unioned.actions.contains(action))
				unioned.actions.add(action);
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
		for (int i = 0; i < actions.size(); i++) {
			if (!actions.get(i).event.equals(otherPlan.actions.get(i).event))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String str = "";
		for (PlanGraphEventNode action : actions)
			str += Text.BLANK + action + "\n";
		return str;
	}
}
