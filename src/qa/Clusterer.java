package qa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class Clusterer {
	
	public RelaxedPlanCluster[] clusters; // array of clusters. Size = k
	public RelaxedPlanVector[] planVecs; // array of vectors representing relaxed plans. Size = n
	public int[] clusterAssignments;
	
	private final int k;
	private final int n;
	
	public Clusterer(RelaxedPlanVector[] planVecs, RelaxedPlanVector[] clusterCentroids) {
		this.planVecs = planVecs;
		this.n = planVecs.length;
		this.k = clusterCentroids.length;
		this.clusters = new RelaxedPlanCluster[k];
		for(int i=0; i<k; i++)
			clusters[i] = new RelaxedPlanCluster(i, clusterCentroids[i]);
		this.clusterAssignments = new int[planVecs.length];
	}

	/*boolean testEquals(int[] one, int[] two) {
		if(one==null || two==null)
			return false;
		if(one.length != two.length) 
			return false;
		for(int i=0; i<one.length; i++) {
			if(one[i]!=two[i])
				return false;
		}
		return true;
	}*/

	/** Find the planVecs that are assigned to this cluster */
	private ArrayList<RelaxedPlanVector> getAssignments(RelaxedPlanCluster cluster){
		ArrayList<RelaxedPlanVector> assigned = new ArrayList<>();
		for(int i=0; i<planVecs.length; i++) {
			if(planVecs[i].clusterAssignment == cluster.getID())
				assigned.add(planVecs[i]);
		}
		return assigned;
	}

	/** Set the cluster centroid to the mean of its current assignments */
	private void updateCentroid(RelaxedPlanCluster cluster) {
		cluster.setCentroid(RelaxedPlanVector.mean(getAssignments(cluster)));
	}

	public void kmeans() {	
		int assignmentsChanged;
		int prevAssignmentsChanged = 0;
		int countSameNumberAssignmentsChanged = 0;
		do{
			assignmentsChanged = 0;
			// Update cluster centroids to reflect their current assignments
			for(RelaxedPlanCluster cluster : clusters)
				updateCentroid(cluster);
			// Update assignment for each planVec
			for(int i=0; i<planVecs.length; i++) {
				float minDistance = Float.MAX_VALUE;
				int assignment = -1;
				for(int c=0; c<k; c++) {
					float distance = planVecs[i].actionDistance(clusters[c].getCentroid());
					if(distance < minDistance) {
						minDistance = distance;
						assignment = c;
					}
				}
				if(planVecs[i].clusterAssignment != assignment) {
					planVecs[i].clusterAssignment = assignment;
					assignmentsChanged++;
				}
			}
			System.out.println("changed " + assignmentsChanged + " assignments");
			if(assignmentsChanged == prevAssignmentsChanged)
				countSameNumberAssignmentsChanged++;
			else {
				prevAssignmentsChanged = assignmentsChanged;
				countSameNumberAssignmentsChanged = 0;
			}
			
			/*for(RelaxedPlanCluster cluster : clusters) {
				System.out.println("Cluster " + cluster.getID() + "\n-- centroid: " + cluster.getCentroid());
				System.out.println("... Events: " + cluster.getCentroid().getActions().toString());
				System.out.println("... Assignments: " + getAssignments(cluster).size());
			}*/

		} while (assignmentsChanged > 0 && countSameNumberAssignmentsChanged < 100);
	}	
}

