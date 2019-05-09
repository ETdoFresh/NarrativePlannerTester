package qa;

import java.util.ArrayList;

public class RelaxedPlanCluster {
	
	protected int id;
	protected RelaxedPlanVector centroid;
	protected RelaxedPlan medoid;
	protected ArrayList<RelaxedPlan> plans = new ArrayList<>();
	
	public RelaxedPlanCluster(int id, int n){
		this.id = id;
		this.centroid = new RelaxedPlanVector(n, 0);
	}
	
	public RelaxedPlanCluster clone() {
		RelaxedPlanCluster clone = new RelaxedPlanCluster(id, 0);
		clone.medoid = medoid;
		clone.plans = new ArrayList<>(plans);
		return clone;
	}
	
	@Override
	public String toString() {
		return "Cluster " + id + ": Medoid: " + medoid.toString();
	}
}