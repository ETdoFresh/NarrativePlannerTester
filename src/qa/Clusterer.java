package qa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class Clusterer {
	
	public RelaxedPlanCluster[] clusters; // array of clusters. Size = k
	public RelaxedPlanVector[] planVecs; // array of vectors representing relaxed plans. Size = n
	
	private final int k;
	private final int n;
	
	public Clusterer(RelaxedPlanVector[] planVecs, int k) {
		this.planVecs = planVecs;
		this.n = planVecs.length;
		this.k = k;
		this.clusters = new RelaxedPlanCluster[k];
		for(int i=0; i<k; i++)
			clusters[i] = new RelaxedPlanCluster(i);
	}

	/** Find the planVecs that are assigned to this cluster */
	protected ArrayList<RelaxedPlanVector> getAssignments(int clusterID){
		ArrayList<RelaxedPlanVector> assigned = new ArrayList<>();
		for(int i=0; i<planVecs.length; i++) {
			if(planVecs[i].clusterAssignment == clusterID)
				assigned.add(planVecs[i]);
		}
		return assigned;
	}

	/** Set the cluster centroid to the mean of its current assignments */
	private void updateCentroid(RelaxedPlanCluster cluster) {
		cluster.setCentroid(RelaxedPlanVector.mean(getAssignments(cluster.getID())));
		System.out.println("Mean of: " + getAssignments(cluster.getID()) + " = " + cluster.getCentroid());
	}

	public void kmeans() {	
		int assignmentsChanged;
		do{
			for(RelaxedPlanCluster cluster : clusters) {
				if(cluster.getCentroid()!=null) {
					System.out.println("Cluster " + cluster.getID() + "\n-- centroid: " + cluster.getCentroid());
					System.out.println("... Events: " + cluster.getCentroid().getActions().toString());
					System.out.println("... # Assignments: " + getAssignments(cluster.getID()).size());
				}
			}

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
		} while (assignmentsChanged > 0);
	}	
}

