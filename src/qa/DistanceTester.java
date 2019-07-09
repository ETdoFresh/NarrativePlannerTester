package qa;

import sabre.space.SearchSpace;

public class DistanceTester {
	private SearchSpace space;
	
	public DistanceTester(SearchSpace space) {
		this.space = space;
	}
	
	public void testDistances(RelaxedPlan a, RelaxedPlan b) {
		System.out.println("Testing distances between plans: \n\tA:\n" 
					+ a.shortString() + "\n\tB:\n" + b.shortString());
		for(DistanceMetric metric : DistanceMetric.values()) {
			Distance d = new Distance(metric, space);
			System.out.println(metric + " Distance = " + d.getDistance(a, b));
		}
	}
}
