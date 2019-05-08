package qa;

import java.util.HashSet;

import sabre.Action;
import sabre.Agent;
import sabre.Event;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Expression;
import sabre.logic.Literal;
import sabre.logic.Term;
import sabre.space.SearchSpace;

public class DomainSet {
	private static SearchSpace space;
	private static HashSet<Event> allActions;
	private static HashSet<SSGPair> allSSGPairs;
	private static HashSet<SSSGPair> allSSSGPairs;
	private static HashSet<AgentSchemaPair> allAgentSchemaPairs;
	private static HashSet<String> allSchemas;

	public static void Initialize(SearchSpace space) {
		DomainSet.space = space;
	}
	
	public static HashSet<String> getAllSchemas(){
		if(allSchemas != null)
			return allSchemas;
		allSchemas = new HashSet<>();
		for(Action action : space.actions)
			allSchemas.add(action.name);
		return allSchemas;
	}
	
	public static HashSet<Event> getAllActions(){
		if (allActions != null)
			return allActions;
		
		allActions = new HashSet<>();
		for(Action action : space.actions)
			allActions.add(action);
		return allActions;
	}
	
	public static HashSet<SSGPair> getAllSSGPairs() {
		if (allSSGPairs != null)
			return allSSGPairs;
		
		allSSGPairs = new HashSet<>();
		for(Action action : space.actions) {			
			for(Expression goal : AgentGoal.getCombinedAuthorAndAllAgentGoals(space.domain)) {
				for(ConjunctiveClause clause : goal.toDNF().arguments) {
					for(Literal goalLiteral : clause.arguments) {
						for(ConjunctiveClause effect : action.effect.toDNF().arguments) {
							for(Literal effectLiteral : effect.arguments) {
								if(CheckEquals.Literal(goalLiteral, effectLiteral))
									allSSGPairs.add(new SSGPair(action, goalLiteral));
							}
						}
					}
				}
			}
		}
		return allSSGPairs;
	}

	public static HashSet<SSSGPair> getAllSSSGPairs() {
		if (allSSSGPairs != null)
			return allSSSGPairs;

		allSSSGPairs = new HashSet<>();
		for (Action action : space.actions) {
			for (Expression goal : AgentGoal.getCombinedAuthorAndAllAgentGoals(space.domain)) {
				for (ConjunctiveClause clause : goal.toDNF().arguments) {
					for (Literal goalLiteral : clause.arguments) {
						for (ConjunctiveClause effect : action.effect.toDNF().arguments) {
							for (Literal effectLiteral : effect.arguments) {
								if (CheckEquals.Literal(goalLiteral, effectLiteral))
									allSSSGPairs.add(new SSSGPair(goalLiteral, action.name));
							}
						}
					}
				}
			}
		}
		return allSSSGPairs;
	}

	public static HashSet<AgentSchemaPair> getAllAgentSchemaPairs() {
		if (allAgentSchemaPairs != null)
			return allAgentSchemaPairs;

		allAgentSchemaPairs = new HashSet<>();
		for (Action action : space.actions) {
			for (Term agent : action.agents)
				if (agent instanceof Agent)
					allAgentSchemaPairs.add(new AgentSchemaPair(((Agent) agent).name, action.name));
		}
		return allAgentSchemaPairs;
	}
}
