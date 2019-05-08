package qa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import sabre.Action;
import sabre.Event;
import sabre.graph.PlanGraphActionNode;
import sabre.graph.PlanGraphAxiomNode;
import sabre.graph.PlanGraphEventNode;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Expression;
import sabre.logic.Literal;
import sabre.space.SearchSpace;
import sabre.state.MutableArrayState;

public class RelaxedPlan implements Iterable<RelaxedNode>, Serializable {
	private static final long serialVersionUID = 1L;
	private ArrayList<RelaxedNode> nodes = new ArrayList<>();
	public ArrayList<Explanation> explanations = new ArrayList<>();
	public ArrayList<RelaxedNode> importantSteps = new ArrayList<>();
	public int clusterAssignment = -1;

	public RelaxedPlan clone() {
		RelaxedPlan clone = new RelaxedPlan();
		clone.nodes.addAll(nodes);
		clone.explanations.addAll(explanations);
		return clone;
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
		for (RelaxedNode node : nodes)
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

	public static RelaxedPlan medoid(ArrayList<RelaxedPlan> plans, Distance distance) {
		RelaxedPlan medoid = null;
		float[] averageDistances = new float[plans.size()];
		SearchSpace space = null;
		for (int i = 0; i < plans.size(); i++) {
			float sum = 0;
			if (space == null)
				space = plans.get(i).nodes.get(0).eventNode.graph.space;
			for (RelaxedPlan other : plans)
				sum += distance.getDistance(plans.get(i), other, plans);
			averageDistances[i] = sum / plans.size();
		}
		float minDistance = Float.MAX_VALUE;
		for (int i = 0; i < plans.size(); i++) {
			if (averageDistances[i] < minDistance 
			|| (averageDistances[i] == minDistance && plans.get(i).size() < medoid.size())) {
				minDistance = averageDistances[i];
				medoid = plans.get(i).clone();
			}
		}
		if (medoid == null)
			return new RelaxedPlan();
		return medoid;
	}

	public void updateExplanations() {
		if (this.explanations != null)
			for (RelaxedNode node : nodes)
				if (node.explanations != null)
					this.explanations.addAll(node.explanations);
	}

	public void updateImportantSteps(SearchSpace space) {
		int[] causalDegrees = new int[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
			causalDegrees[i] = getCausalDegree(nodes.get(i), space.goal);
		int maxCausalDegree = 0;
		for (int i = 0; i < nodes.size(); i++)
			if (causalDegrees[i] > maxCausalDegree)
				maxCausalDegree = causalDegrees[i];
		for (int i = 0; i < nodes.size(); i++)
			if (causalDegrees[i] == maxCausalDegree)
				importantSteps.add(nodes.get(i));
	}

	/**
	 * Get the causal degree of a step in this RelaxedPlan, where: causal degree =
	 * +1 for each effect of a previous action that this step "uses" in its
	 * preconditions +1 for each precondition of a later action that this step
	 * achieves +1 for each literal of the problem goal that this step achieves
	 * 
	 * @param node - the step
	 * @param goal - the problem goal
	 * @return degree
	 */
	public int getCausalDegree(RelaxedNode node, Expression goal) {
		int degree = 0;
		if (node.eventNode instanceof PlanGraphAxiomNode)
			return degree;
		int index = nodes.indexOf(node);
		for (int i = 0; i < index; i++)
			for (ConjunctiveClause effect : nodes.get(i).eventNode.event.effect.toDNF().arguments)
				for (Literal e_literal : effect.arguments)
					for (ConjunctiveClause precondition : node.eventNode.event.precondition.toDNF().arguments)
						for (Literal p_literal : precondition.arguments)
							if (CheckEquals.Literal(e_literal, p_literal))
								degree++;
		for (int i = index; i < nodes.size(); i++)
			for (ConjunctiveClause precondition : nodes.get(i).eventNode.event.precondition.toDNF().arguments)
				for (Literal p_literal : precondition.arguments)
					for (ConjunctiveClause effect : node.eventNode.event.effect.toDNF().arguments)
						for (Literal e_literal : effect.arguments)
							if (CheckEquals.Literal(p_literal, e_literal))
								degree++;
		for (ConjunctiveClause conjunct : goal.toDNF().arguments)
			for (Literal g_literal : conjunct.arguments)
				for (ConjunctiveClause effect : node.eventNode.event.effect.toDNF().arguments)
					for (Literal e_literal : effect.arguments)
						if (CheckEquals.Literal(e_literal, g_literal))
							degree++;
		return degree;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof RelaxedPlan))
			return false;
		RelaxedPlan otherPlan = (RelaxedPlan) other;
		if (size() != otherPlan.size())
			return false;

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
		
		str += Text.BLANK + "-- SSGPair ---------------------------\n";
		for (SSGPair pair : getSSGPairs())
			str += Text.BLANK + pair + "\n";
		
		str += Text.BLANK + "-- SSSGPair ---------------------------\n";
		for (SSSGPair pair : SSSGPair.GetByPlan(this))
			str += Text.BLANK + pair + "\n";
		
		str += Text.BLANK + "-- Schema ---------------------------\n";
		str += Text.BLANK + getSchemas() + "\n";
		
		str += Text.BLANK + "-- AgentSchema Pair ---------------------------\n";
		str += Text.BLANK + getAgentSchemaPairs() + "\n";
		
		return str;
	}

	public void pushAll(HashSet<RelaxedNode> set) {
		for (RelaxedNode node : set)
			push(node);
	}

	public void remove(int i) {
		nodes.remove(i);
	}
	
	public HashSet<AgentSchemaPair> getAgentSchemaPairs(){
		HashSet<AgentSchemaPair> allPairs = new HashSet<>();
		for(RelaxedNode node : this) {
			for(sabre.Agent agent : node.consenting) {
				allPairs.add(new AgentSchemaPair(agent.toString(), node.schema));
			}
		}
		return allPairs;
	}
	
	public HashSet<SSGPair> getSSGPairs() {
		HashSet<SSGPair> allPairs = new HashSet<>();
		for (RelaxedNode node : this)
			allPairs.addAll(node.satisfyingStepGoalLiteralPairs);
		return allPairs;
	}
	
	public HashSet<String> getSchemas() {
		HashSet<String> allPairs = new HashSet<>();
		for (RelaxedNode node : this)
			allPairs.add(node.schema);
		return allPairs;
	}
}
