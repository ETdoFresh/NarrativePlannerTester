package qa;

public class RelaxedPlanCluster {
	
	private int id;
	private RelaxedPlanVector centroid;
	
	public RelaxedPlanCluster(int id){
		this.id = id;
	}
	
	public int getID() {
		return id;
	}
	
	public RelaxedPlanVector getCentroid() {
		return this.centroid;
	}
	
	public void setCentroid(RelaxedPlanVector centroid) {
		this.centroid = centroid;
	}
	
	@Override
	public String toString() {
		return "Cluster " + id + ":\n  Centroid: " + centroid.toString();
	}
}