package qa;

import java.util.ArrayList;
import java.util.HashSet;

import sabre.Agent;
import sabre.Domain;
import sabre.logic.Assignment;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Expression;
import sabre.logic.Literal;

public class AgentGoal {
	public static Expression get(Domain domain, Agent agent) {
		if (agent.name.equals("Author"))
			return domain.goal;
		
		ArrayList<Literal> goals = new ArrayList<>();
		for (Expression expression : domain.initial) {
			if (expression instanceof Assignment) {
				Assignment assignment = (Assignment) expression;
				if (assignment.property.name == "intends") {
					Agent expressionAgent = (Agent) assignment.arguments.get(0);
					if (expressionAgent.equals(agent))
						goals.add((Literal) assignment.arguments.get(1));
				}
			}
		}
		return new ConjunctiveClause(goals);
	}

	public static Iterable<Expression> getAllAgentGoals(Domain domain) {
		ArrayList<Expression> agentGoals = new ArrayList<>();
		for (Agent agent : domain.agents)
			agentGoals.add(get(domain, agent));
		return agentGoals;
	}

	public static Iterable<Literal> getCombinedAuthorAndAllAgentGoals(Domain domain) {
		HashSet<Literal> literals = new HashSet<>();
		for (Expression goals : getAllAgentGoals(domain))
			for (ConjunctiveClause goal : goals.toDNF().arguments)
				for (Literal literal : goal.arguments)
					literals.add(literal);

		for (ConjunctiveClause goal : domain.goal.toDNF().arguments)
			for (Literal literal : goal.arguments)
				literals.add(literal);

		return literals;
	}

	public static Expression get(Domain domain, String agent) {
		return get(domain, domain.getAgent(agent));
	}
}
