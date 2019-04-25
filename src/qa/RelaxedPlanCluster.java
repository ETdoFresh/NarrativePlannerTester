package qa;

public class RelaxedPlanCluster {
	
	protected int id;
	protected RelaxedPlanVector centroid;
	protected RelaxedPlan medoid;
	
	public RelaxedPlanCluster(int id, int n){
		this.id = id;
		this.centroid = new RelaxedPlanVector(n, 0);
	}
	
	public RelaxedPlanCluster clone() {
		RelaxedPlanCluster clone = new RelaxedPlanCluster(id, 0);
		clone.medoid = medoid;
		return clone;
	}
	
	@Override
	public String toString() {
		return "Cluster " + id + ": Medoid: " + medoid.toString();
	}
}