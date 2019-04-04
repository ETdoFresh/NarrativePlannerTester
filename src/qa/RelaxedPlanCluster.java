package qa;

public class RelaxedPlanCluster {
	
	protected int id;
	protected RelaxedPlanVector centroid;
	protected RelaxedPlan medoid;
	
	public RelaxedPlanCluster(int id){
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "Cluster " + id + ": Centroid: " + centroid.toString();
	}
}