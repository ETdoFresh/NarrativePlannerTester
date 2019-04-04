package qa;

import java.util.ArrayList;

import sabre.Agent;
import sabre.Domain;
import sabre.logic.Assignment;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Expression;
import sabre.logic.Literal;

public class AgentGoal {
	public static Expression get(Domain domain, Agent agent) {
		ArrayList<Literal> goals = new ArrayList<>();
		for(Expression expression : domain.initial) {
			if (expression instanceof Assignment) {
				Assignment assignment = (Assignment)expression;
				if (assignment.property.name == "intends") {
					Agent expressionAgent = (Agent)assignment.arguments.get(0);
					if (expressionAgent.equals(agent))
						goals.add((Literal)assignment.arguments.get(1));
				}
			}
		}
		return new ConjunctiveClause(goals);
	}
	
	public static Iterable<Expression> getAll(Domain domain) {
		ArrayList<Expression> agentGoals = new ArrayList<>();
		for (Agent agent : domain.agents)
			agentGoals.add(get(domain, agent));
		return agentGoals;
	}
	
	public static Expression get(Domain domain, String agent) {
		return get(domain, domain.getAgent(agent));
	}
}
