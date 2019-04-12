package qa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class Clusterer {
	
	public RelaxedPlanCluster[] clusters; // array of clusters. Size = k
	public RelaxedPlanVector[] planVecs; // array of vectors representing relaxed plans. Size = n
	public RelaxedPlan[] relaxedPlans;
	
	private final int k;

	protected final int n;
	
	public Clusterer(RelaxedPlanVector[] planVecs, int k, int n) {
		this.planVecs = planVecs;
		this.k = k;
		this.n = n;
		this.clusters = new RelaxedPlanCluster[k];
		System.out.println("About to initialize clusters in Clusterer constructor");
		for(int i=0; i<k; i++)
			clusters[i] = new RelaxedPlanCluster(i, n);
	}
	
	public Clusterer(RelaxedPlan[] relaxedPlans, int k, int n) {
		this.relaxedPlans = relaxedPlans;
		this.k = k;
		this.n = n;
		this.clusters = new RelaxedPlanCluster[k];
		for(int i=0; i<k; i++)
			clusters[i] = new RelaxedPlanCluster(i, n);
	}

	/** Find the planVecs that are assigned to this cluster */
	protected ArrayList<RelaxedPlanVector> getVectorAssignments(int clusterID){
		ArrayList<RelaxedPlanVector> assigned = new ArrayList<>();
		for(int i=0; i<planVecs.length; i++) {
			if(planVecs[i].clusterAssignment == clusterID)
				assigned.add(planVecs[i]);
		}
		return assigned;
	}

	protected ArrayList<RelaxedPlan> getPlanAssignments(int clusterID){
		ArrayList<RelaxedPlan> assigned = new ArrayList<>();
		for(int i=0; i<relaxedPlans.length; i++) {
			if(relaxedPlans[i].clusterAssignment == clusterID)
				assigned.add(relaxedPlans[i]);
		}
		return assigned;
	}

	
	/** Set the cluster centroid to the mean of its current assignments */
	private void updateCentroid(RelaxedPlanCluster cluster) {
		cluster.centroid = RelaxedPlanVector.mean(getVectorAssignments(cluster.id));
	}
	
	private void updateMedoid(RelaxedPlanCluster cluster) {
		cluster.centroid = RelaxedPlanVector.medoid(getVectorAssignments(cluster.id), n);
	}

	private void updateMedoid(RelaxedPlanCluster cluster, boolean withoutVectors) {
		cluster.medoid = RelaxedPlan.medoid(getPlanAssignments(cluster.id));
	}
	
	public void kmedoids() {
		System.out.println("K-MEDOIDS (using vectors): ");
		int iteration = 1;
		int assignmentsChanged;
		do {
			assignmentsChanged = 0;
			// Update medoids
			for(RelaxedPlanCluster cluster : clusters)
				updateMedoid(cluster);
			// Update assignments
			for(int i=0; i<planVecs.length; i++) {
				float minDistance = Float.MAX_VALUE;
				int clusterToAssign = -1;
				for(int c=0; c<k; c++) {
					float distance = planVecs[i].jaccard(clusters[c].centroid);
					if(distance < minDistance) {
						minDistance = distance;
						clusterToAssign = c;
					}
				}
				if(planVecs[i].clusterAssignment != clusterToAssign) {
					planVecs[i].clusterAssignment = clusterToAssign;
					assignmentsChanged++;
				}
			}
			System.out.println("Iteration " + iteration + " changed " + assignmentsChanged + " assignments.");
			iteration++;
		} while(assignmentsChanged > 0);
	}
	
	public void kmedoids(boolean withoutVectors) {
		System.out.println("K-MEDOIDS (with RelaxedPlans, no vectors): ");
		int iteration = 1;
		int assignmentsChanged;
		do {
			assignmentsChanged = 0;
			// Update medoids
			for(RelaxedPlanCluster cluster : clusters)
				updateMedoid(cluster, withoutVectors);
			// Update assignments
			for(int i=0; i<relaxedPlans.length; i++) {
				float minDistance = Float.MAX_VALUE;
				int clusterToAssign = -1;
				for(int c=0; c<k; c++) {
					float distance = relaxedPlans[i].actionDistance(clusters[c].medoid);
					if(distance < minDistance) {
						minDistance = distance;
						clusterToAssign = c;
					}
				}
				if(relaxedPlans[i].clusterAssignment != clusterToAssign) {
					relaxedPlans[i].clusterAssignment = clusterToAssign;
					assignmentsChanged++;
				}
			}
			System.out.println("Iteration " + iteration + " changed " + assignmentsChanged + " assignments.");
			iteration++;
		} while(assignmentsChanged > 0);
	}
	
	public void kmeans() {
		System.out.println("K-MEANS: ");
		int iteration = 1;
		int assignmentsChanged;
		do{
			assignmentsChanged = 0;

			/*for(RelaxedPlanCluster cluster : clusters) {
				if(cluster.getCentroid()!=null) {
					System.out.println("Cluster " + cluster.getID() + "\n-- centroid: " + cluster.getCentroid());
					System.out.println("... Events: " + cluster.getCentroid().toActionList().toString());
					System.out.println("... # Assignments: " + getAssignments(cluster.getID()).size());
				}
			}*/

			// Update cluster centroids to reflect their current assignments
			for(RelaxedPlanCluster cluster : clusters)
				updateCentroid(cluster);
			
			// Update assignment for each planVec
			for(int i=0; i<planVecs.length; i++) {
				float minDistance = Float.MAX_VALUE;
				int clusterToAssign = -1;
				for(int c=0; c<k; c++) {
					float distance = planVecs[i].jaccard(clusters[c].centroid);
					if(distance < minDistance) {
						minDistance = distance;
						clusterToAssign = c;
					}
				}
				if(planVecs[i].clusterAssignment != clusterToAssign) {
					planVecs[i].clusterAssignment = clusterToAssign;
					assignmentsChanged++;
				}
			}
			System.out.println("Iteration " + iteration +" changed " + assignmentsChanged + " assignments.");			
			iteration++;
		} while (assignmentsChanged > 0);
	}	
}

