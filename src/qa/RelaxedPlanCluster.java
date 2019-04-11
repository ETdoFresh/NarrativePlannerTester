package qa;

public class RelaxedPlanCluster {
	
	protected int id;
	protected RelaxedPlanVector centroid;
	protected RelaxedPlan medoid;
	
	public RelaxedPlanCluster(int id, int n){
		this.id = id;
		this.centroid = new RelaxedPlanVector(n, 0);
	}
	
	@Override
	public String toString() {
		return "Cluster " + id + ": Centroid: " + centroid.toString();
	}
}