package qa;

import java.util.ArrayList;

public class RelaxedPlanCluster {
	
	protected int id;
	protected float[] centroid;
	protected RelaxedPlanVector rpvCentroid;
	protected RelaxedPlan medoid;
	protected ArrayList<RelaxedPlan> plans = new ArrayList<>();
	public int size;
	public float averageDistance;
	
	public RelaxedPlanCluster(int id, int n){
		this.id = id;
		this.centroid = new float[n];
	}
	
	public RelaxedPlanCluster clone() {
		RelaxedPlanCluster clone = new RelaxedPlanCluster(id, 0);
		clone.medoid = medoid;
		clone.plans = new ArrayList<>(plans);
		clone.size = size;
		clone.averageDistance = averageDistance;
		return clone;
	}
	
	@Override
	public String toString() {
		return "Cluster " + id + ": Medoid: " + medoid.toString();
	}
}