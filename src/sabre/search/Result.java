package sabre.search;

import sabre.Plan;

public class Result {

	public final Plan plan;
	public final long time;
	public final int visited;
	public final int generated;
	public final int pruned;
	
	Result(Plan plan, long time, int visited, int generated, int pruned) {
		this.plan = plan;
		this.visited = visited;
		this.generated = generated;
		this.pruned = pruned;
		this.time = time;
	}
	
	@Override
	public String toString() {
		String string = "[Result: ";
		if(plan == null)
			string += "failure";
		else
			string += "success";
		string += "; " + time + " ms";
		string += "; " + visited + " visited";
		string += "; " + generated + " generated";
		string += "; " + pruned + " pruned";
		return string + "]";
	}
}
