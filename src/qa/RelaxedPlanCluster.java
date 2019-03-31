package qa;

public class RelaxedPlanCluster {
	
	private int id;
	private RelaxedPlanVector centroid;
	//private ArrayList<RelaxedPlanVector> assignments = new ArrayList<>();
	
	public RelaxedPlanCluster(int id, RelaxedPlanVector centroid){
		this.id = id;
		this.centroid = centroid;
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
	
	/*public void assign(RelaxedPlanVector point) {
		this.assignments.add(point);
	}*/
	
	/*public boolean remove(RelaxedPlanVector point) {
		if(assignments.contains(point)) {
			assignments.remove(point);
			return true;
		} else
			return false;
	}*/

	/*public boolean contains(RelaxedPlanVector point) {
		return assignments.contains(point);
	}
	public int size() {
		return assignments.size();
	}*/
	
	/*public ArrayList<RelaxedPlanVector> getAssignments(){
		return this.assignments;
	}*/
			
	/*public void updateCentroid() {
		this.centroid = mean();
	}*/
	
/*	public void updateCentroid() {
		if(assignments==null || assignments.size()==0)
			return;
		for(int i=0; i<centroid.size; i++) {
			float sum = 0;
			for(int j=0; j<assignments.size(); j++) {
				if(assignments.get(j).actionValues[i])
					sum++;
			}
			if(sum/assignments.size() >= 0.5f)
				centroid.actionValues[i] = true;
			else
				centroid.actionValues[i] = false;
		}
	}
*/	
	@Override
	public String toString() {
		String s = "Cluster " + id + ":\n  Centroid: " + centroid.toString();
		/*s += "\n  Assignments:\n";
		for(RelaxedPlanVector assignment : assignments) {
			s += assignment.toString()+"\n";
		}*/
		return s;
	}
}