package qa;

import java.util.ArrayList;

import sabre.space.SearchSpace;

public class Clusterer {
	
	public RelaxedPlanCluster[] clusters; // array of clusters. Size = k
	public RelaxedPlanVector[] planVecs; // array of vectors representing relaxed plans. Size = n
	public ArrayList<RelaxedPlan> relaxedPlans;
	public Distance distance;
	
	private final int k;

	protected final int n;
	
	private SearchSpace space;
	
	public Clusterer(ArrayList<RelaxedPlan> relaxedPlans, int k, int n, SearchSpace space, Distance distance) {
		this.space = space;
		this.relaxedPlans = relaxedPlans;
		this.k = k;
		this.n = n;
		this.clusters = new RelaxedPlanCluster[k];
		for(int i=0; i<k; i++)
			clusters[i] = new RelaxedPlanCluster(i, n);
		this.distance = distance;
		for(RelaxedPlan plan : relaxedPlans) {
			plan.updateExplanations();
			plan.updateImportantSteps(space);
		}
	}
		
	private Clusterer(Clusterer clusterer) {
		space = clusterer.space;
		relaxedPlans = clusterer.relaxedPlans;
		k = clusterer.k;
		n = clusterer.n;
		clusters = new RelaxedPlanCluster[k];
		for(int i=0; i<k; i++)
			clusters[i] = clusterer.clusters[i].clone();
		distance = clusterer.distance;
	}
	
	public Clusterer clone() {
		return new Clusterer(this);
	}

	/** For vectors **/
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
		return clusters[clusterID].plans;
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
			ArrayList<RelaxedPlan> assignments = clusters[cluster.id].plans;
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
		int iteration = 1;
		int assignmentsChanged;
		do {
			assignmentsChanged = 0;			
			System.out.println("Iteration " + iteration + ": Updating medoids");
			// Update medoids
			for(RelaxedPlanCluster cluster : clusters)
				//cluster.medoid = RelaxedPlan.medoid(getAssignments(cluster.id), distance);
				cluster.medoid = RelaxedPlan.medoid(cluster.plans, distance);
			
			System.out.println("Iteration " + iteration + ": Updating assignments");
			// Update assignments
			for(int i=0; i<relaxedPlans.size(); i++) {
				float minDistance = Float.MAX_VALUE;
				int clusterToAssign = -1;
				for(int c=0; c<k; c++) {
					float dist = distance.getDistance(relaxedPlans.get(i), clusters[c].medoid);
					if(dist < minDistance) {
						minDistance = dist;
						clusterToAssign = c;
					}
				}
				
				//if(relaxedPlans.get(i).clusterAssignment != clusterToAssign) {
				//	relaxedPlans.get(i).clusterAssignment = clusterToAssign;
				if (!clusters[clusterToAssign].plans.contains(relaxedPlans.get(i))) {
					for (int j = 0; j < k; j++)
						clusters[j].plans.remove(relaxedPlans.get(i));
					clusters[clusterToAssign].plans.add(relaxedPlans.get(i));
					assignmentsChanged++;
				}
			}
			//System.out.println("Iteration " + iteration + " changed " + assignmentsChanged + " assignments.");
			iteration++;
		} while(assignmentsChanged > 0);
	}
	
	public void kmedoidsWithVectors() {
		//System.out.println("K-MEDOIDS (using vectors): ");
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
			//System.out.println("Iteration " + iteration + " changed " + assignmentsChanged + " assignments.");
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
			//System.out.println("Iteration " + iteration +" changed " + assignmentsChanged + " assignments.");			
			iteration++;
		} while (assignmentsChanged > 0);
	}	
	
	public boolean HasEmptyCluster() {
		for (RelaxedPlanCluster cluster : clusters)
			if (cluster.plans.isEmpty())
				return true;
		
		return false;
	}
	
	@Override
	public String toString() {
		String s = "";
		for (int i = 0; i < k; i++)
			s += "Cluster " + i + " (" + clusters[i].plans.size() + " assignments):\n" 
					+ clusters[i].medoid.shortString() + "\n";
		return s;
	}
}

