package qa;

import java.util.ArrayList;
import sabre.Action;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Expression;

public class Explanation {
	public RelaxedPlan plan;
	public Expression goals;
	public ExplanationGraph graph;
	
	public Explanation(RelaxedPlan plan, Expression goals) {
		this.plan = plan;
		this.goals = goals;
		this.graph = new ExplanationGraph();
	}
	
	public static boolean IsValid(RelaxedPlan plan, Expression goals) {
		Explanation explanation = new Explanation(plan, goals);
		for (ConjunctiveClause goal : goals.toDNF().arguments) {
			explanation.graph.GenerateEdgesFromGoal(goal);
			if (!explanation.graph.hasGeneratedSuccesfully())
				return false;
		}
		return true;
	}
	
	public class ExplanationGraph{
		public ArrayList<ExplanationNode> nodes;
		public ArrayList<ExplanationEdge> edges;
		
		public void GenerateEdgesFromGoal(ConjunctiveClause goal) {
		}

		public boolean hasGeneratedSuccesfully() {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	public class ExplanationNode{
		public Action action;
	}
	
	public class ExplanationEdge{
		public ExplanationNode child;
		public ExplanationNode parent;
	}
}
