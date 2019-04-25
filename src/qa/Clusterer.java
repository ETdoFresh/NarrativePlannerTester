package qa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Function;

import sabre.space.SearchSpace;

public class Clusterer {
	
	public RelaxedPlanCluster[] clusters; // array of clusters. Size = k
	public RelaxedPlanVector[] planVecs; // array of vectors representing relaxed plans. Size = n
	public ArrayList<RelaxedPlan> relaxedPlans;
	private Distance distance;
	
	private final int k;

	protected final int n;
	
	private SearchSpace space;
	
	/** Cluster RelaxedPlans **/
	public Clusterer(ArrayList<RelaxedPlan> relaxedPlans, int k, int n, SearchSpace space, DistanceMetric metric) {
		this.space = space;
		this.relaxedPlans = relaxedPlans;
		this.k = k;
		this.n = n;
		this.clusters = new RelaxedPlanCluster[k];
		for(int i=0; i<k; i++)
			clusters[i] = new RelaxedPlanCluster(i, n);
		this.distance = new Distance(metric, space);
		deDupePlans();
	}
	
	/** Remove duplicate RelaxedPlans according to current distance metric */
	private void deDupePlans() {
		int previous = relaxedPlans.size();
		ArrayList<RelaxedPlan> uniquePlans = new ArrayList<>();
		for(RelaxedPlan plan : relaxedPlans) {
			boolean duplicate = false;
			for(RelaxedPlan existingPlan : uniquePlans) {
				if(distance.getDistance(plan, existingPlan, uniquePlans) == 0) {
					duplicate = true;
					break;
				}
			}
			if(!duplicate)
				uniquePlans.add(plan);
		}
		this.relaxedPlans = uniquePlans;
		System.out.println("Deduping: Had " + previous +" plans, now have " + relaxedPlans.size());
	}

	/** Cluster vectors **/
	public Clusterer(RelaxedPlanVector[] planVecs, int k, int n, SearchSpace space, DistanceMetric metric) {
		this.planVecs = planVecs;
		this.k = k;
		this.n = n;
		this.clusters = new RelaxedPlanCluster[k];
		for(int i=0; i<k; i++)
			clusters[i] = new RelaxedPlanCluster(i, n);
		this.distance = new Distance(metric, space);
	}
	
	/** Get all RelaxedPlans that are assigned to a cluster */
	protected ArrayList<RelaxedPlan> getAssignments(int clusterID){
		ArrayList<RelaxedPlan> assigned = new ArrayList<>();
		for(int i=0; i<relaxedPlans.size(); i++) {
			if(relaxedPlans.get(i).clusterAssignment == clusterID)
				assigned.add(relaxedPlans.get(i));
		}
		return assigned;
	}

	/** Get all vectors that are assigned to a cluster */
	protected ArrayList<RelaxedPlanVector> getAssignmentsWithVectors(int clusterID){
		ArrayList<RelaxedPlanVector> assigned = new ArrayList<>();
		for(int i=0; i<planVecs.length; i++) {
			if(planVecs[i].clusterAssignment == clusterID)
				assigned.add(planVecs[i]);
		}
		return assigned;
	}

	// TODO: Differently
	public RelaxedPlan[] getExemplars() {
		RelaxedPlan[] plans = new RelaxedPlan[k];
		for(RelaxedPlanCluster cluster : clusters) {
			RelaxedPlan exemplar = cluster.medoid.clone();
			ArrayList<RelaxedPlan> assignments = getAssignments(cluster.id);
			while(!exemplar.isValid(space)) {
				assignments.remove(exemplar);
				if(assignments.isEmpty())
					break;
				exemplar = RelaxedPlan.medoid(assignments, distance);
			}
			// if the exemplar is valid, put it in the array; otherwise put a null plan
			if(exemplar.isValid(space))
				plans[cluster.id] = exemplar.clone();
			else
				plans[cluster.id] = null;
		}
		return plans;
	}

	public void kmedoids() {
		System.out.println("K-MEDOIDS (with RelaxedPlans, no vectors): ");
		int iteration = 1;
		int assignmentsChanged;
		do {
			assignmentsChanged = 0;
			// Update medoids
			for(RelaxedPlanCluster cluster : clusters)
				cluster.medoid = RelaxedPlan.medoid(getAssignments(cluster.id), distance);
			// Update assignments
			for(int i=0; i<relaxedPlans.size(); i++) {
				float minDistance = Float.MAX_VALUE;
				int clusterToAssign = -1;
				for(int c=0; c<k; c++) {
					float dist = distance.getDistance(relaxedPlans.get(i), clusters[c].medoid, relaxedPlans);
					if(dist < minDistance) {
						minDistance = dist;
						clusterToAssign = c;
					}
				}
				if(relaxedPlans.get(i).clusterAssignment != clusterToAssign) {
					relaxedPlans.get(i).clusterAssignment = clusterToAssign;
					assignmentsChanged++;
				}
			}
			System.out.println("Iteration " + iteration + " changed " + assignmentsChanged + " assignments.");
			iteration++;
		} while(assignmentsChanged > 0);
	}
	
	public void kmedoidsWithVectors() {
		System.out.println("K-MEDOIDS (using vectors): ");
		int iteration = 1;
		int assignmentsChanged;
		do {
			assignmentsChanged = 0;
			// Update medoids
			for(RelaxedPlanCluster cluster : clusters)
				cluster.centroid = RelaxedPlanVector.medoid(getAssignmentsWithVectors(cluster.id), n);
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
				cluster.centroid = RelaxedPlanVector.mean(getAssignmentsWithVectors(cluster.id));
			
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

