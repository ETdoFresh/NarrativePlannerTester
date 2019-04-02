package qa;

import java.util.ArrayList;
import java.util.Iterator;

import sabre.Agent;
import sabre.logic.Assignment;
import sabre.logic.Expression;
import sabre.space.SearchSpace;

public class GoalGraphs implements Iterator<GoalGraph> {
	public ArrayList<GoalGraph> graphs = new ArrayList<>();
	
	public GoalGraphs(SearchSpace space) {
		for(Expression expression : space.domain.initial) {
			if (expression instanceof Assignment) {
				Assignment assignment = (Assignment)expression;
				if (assignment.property.name == "intends") {
					Agent agent = (Agent)assignment.arguments.get(0);
					Assignment agentGoal = (Assignment)assignment.arguments.get(1);
					GoalGraph newGoalGraph = new GoalGraph(agent, agentGoal);
					while (newGoalGraph.extend(space)) {}
					graphs.add(newGoalGraph);
				}
			}
		}
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public GoalGraph next() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		String output = "";
		for(GoalGraph graph : graphs)
			output += graph + "\n";
		return output;
	}
}
