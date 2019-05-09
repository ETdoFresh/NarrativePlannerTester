package qa;

import java.util.ArrayList;

import sabre.space.SearchSpace;

public class Clusterer {

	public RelaxedPlanCluster[] clusters; // array of clusters. Size = k
	public RelaxedPlanVector[] planVecs; // array of vectors representing relaxed plans. Size = n
	public float[][] vectors;
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
		for (int i = 0; i < k; i++)
			clusters[i] = new RelaxedPlanCluster(i, n);
		this.distance = distance;
		for (RelaxedPlan plan : relaxedPlans) {
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
		for (int i = 0; i < k; i++)
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
		for (int i = 0; i < k; i++)
			clusters[i] = new RelaxedPlanCluster(i, n);
		this.distance = new Distance(metric, space);
	}

	/** Get all RelaxedPlans that are assigned to a cluster */
	protected ArrayList<RelaxedPlan> getAssignments(int clusterID) {
		ArrayList<RelaxedPlan> assigned = new ArrayList<>();
		for (int i = 0; i < relaxedPlans.size(); i++) {
			if (relaxedPlans.get(i).clusterAssignment == clusterID)
				assigned.add(relaxedPlans.get(i));
		}
		return assigned;
	}

	/** Get all vectors that are assigned to a cluster */
	protected ArrayList<RelaxedPlanVector> getAssignmentsWithVectors(int clusterID) {
		ArrayList<RelaxedPlanVector> assigned = new ArrayList<>();
		for (int i = 0; i < planVecs.length; i++) {
			if (planVecs[i].clusterAssignment == clusterID)
				assigned.add(planVecs[i]);
		}
		return assigned;
	}

	// TODO: Differently
	public RelaxedPlan[] getExemplars() {
		RelaxedPlan[] plans = new RelaxedPlan[k];
		for (RelaxedPlanCluster cluster : clusters) {
			RelaxedPlan exemplar = cluster.medoid.clone();
			ArrayList<RelaxedPlan> assignments = getAssignments(cluster.id);
			while (!exemplar.isValid(space)) {
				assignments.remove(exemplar);
				if (assignments.isEmpty())
					break;
				exemplar = RelaxedPlan.medoid(assignments, distance);
			}
			// if the exemplar is valid, put it in the array; otherwise put a null plan
			if (exemplar.isValid(space))
				plans[cluster.id] = exemplar.clone();
			else
				plans[cluster.id] = null;
		}
		return plans;
	}

	public void kmedoids() {
		// System.out.println("K-MEDOIDS (with RelaxedPlans, no vectors): ");
		int iteration = 1;
		int assignmentsChanged;
		do {
			assignmentsChanged = 0;

			// Update medoids
			for (RelaxedPlanCluster cluster : clusters)
				// cluster.medoid = RelaxedPlan.medoid(getAssignments(cluster.id), distance);
				cluster.medoid = RelaxedPlan.medoid(cluster.plans, distance);

			// Update assignments
			for (int i = 0; i < relaxedPlans.size(); i++) {
				float minDistance = Float.MAX_VALUE;
				int clusterToAssign = -1;
				for (int c = 0; c < k; c++) {
					float dist = distance.getDistance(relaxedPlans.get(i), clusters[c].medoid);
					if (dist < minDistance) {
						minDistance = dist;
						clusterToAssign = c;
					}
				}

				// if(relaxedPlans.get(i).clusterAssignment != clusterToAssign) {
				// relaxedPlans.get(i).clusterAssignment = clusterToAssign;
				if (!clusters[clusterToAssign].plans.contains(relaxedPlans.get(i))) {
					for (int j = 0; j < k; j++)
						clusters[j].plans.remove(relaxedPlans.get(i));
					clusters[clusterToAssign].plans.add(relaxedPlans.get(i));
					assignmentsChanged++;
				}
			}
			// System.out.println("Iteration " + iteration + " changed " +
			// assignmentsChanged + " assignments.");
			iteration++;
		} while (assignmentsChanged > 0);
	}

	public void kmedoidsWithVectors() {
		// System.out.println("K-MEDOIDS (using vectors): ");
		int iteration = 1;
		int assignmentsChanged;
		do {
			assignmentsChanged = 0;
			// Update medoids
			for (RelaxedPlanCluster cluster : clusters)
				cluster.rpvCentroid = RelaxedPlanVector.medoid(getAssignmentsWithVectors(cluster.id), n);
			// Update assignments
			for (int i = 0; i < planVecs.length; i++) {
				float minDistance = Float.MAX_VALUE;
				int clusterToAssign = -1;
				for (int c = 0; c < k; c++) {
					float distance = planVecs[i].jaccard(clusters[c].rpvCentroid);
					if (distance < minDistance) {
						minDistance = distance;
						clusterToAssign = c;
					}
				}
				if (planVecs[i].clusterAssignment != clusterToAssign) {
					planVecs[i].clusterAssignment = clusterToAssign;
					assignmentsChanged++;
				}
			}
			// System.out.println("Iteration " + iteration + " changed " +
			// assignmentsChanged + " assignments.");
			iteration++;
		} while (assignmentsChanged > 0);
	}

	public void kmeans() {
		int iteration = 1;
		int assignmentsChanged;

		if (vectors == null) {
			vectors = new float[relaxedPlans.size()][];
			for (int i = 0; i < vectors.length; i++)
				vectors[i] = Vector.get(relaxedPlans.get(i), distance);
		}

		do {
			assignmentsChanged = 0;

			// Update cluster centroids to reflect their current assignments
			for (RelaxedPlanCluster cluster : clusters)
				// cluster.centroid =
				// RelaxedPlanVector.mean(getAssignmentsWithVectors(cluster.id));
				cluster.centroid = GetMean(cluster.plans, distance);

			// Update assignment for each planVec
			for (int i = 0; i < vectors.length; i++) {
				float minDistance = Float.MAX_VALUE;
				int clusterToAssign = -1;
				for (int c = 0; c < k; c++) {
					float dist = distance.distanceMetric == DistanceMetric.SATSTEP_GOAL_PAIR_SCHEMAS_WEIGTHED
							? Vector.getWeightedDistance(clusters[c].centroid, vectors[i])
							: Vector.distance(clusters[c].centroid, vectors[i]);
					if (dist < minDistance) {
						minDistance = dist;
						clusterToAssign = c;
					}
				}
//				if(planVecs[i].clusterAssignment != clusterToAssign) {
//					planVecs[i].clusterAssignment = clusterToAssign;
//					assignmentsChanged++;
				if (!clusters[clusterToAssign].plans.contains(relaxedPlans.get(i))) {
					for (int j = 0; j < k; j++)
						clusters[j].plans.remove(relaxedPlans.get(i));
					clusters[clusterToAssign].plans.add(relaxedPlans.get(i));
					assignmentsChanged++;
				}
			}
			// System.out.println("Iteration " + iteration +" changed " + assignmentsChanged
			// + " assignments.");
			iteration++;
		} while (assignmentsChanged > 0);

		for (RelaxedPlanCluster cluster : clusters) {
			float minDistance = Float.POSITIVE_INFINITY;
			for (RelaxedPlan plan : cluster.plans) {
				float dist = distance.distanceMetric == DistanceMetric.SATSTEP_GOAL_PAIR_SCHEMAS_WEIGTHED
						? Vector.getWeightedDistance(cluster.centroid, Vector.get(plan, distance))
						: Vector.distance(cluster.centroid, Vector.get(plan, distance));
				if (minDistance > dist) {
					minDistance = dist;
					cluster.medoid = plan;
				}
			}
		}

		for (RelaxedPlanCluster cluster : clusters) {
			cluster.size = cluster.plans.size();
			cluster.averageDistance = 0;
			for (RelaxedPlan plan : cluster.plans)
				cluster.averageDistance += distance.distanceMetric == DistanceMetric.SATSTEP_GOAL_PAIR_SCHEMAS_WEIGTHED
						? Vector.getWeightedDistance(cluster.centroid, Vector.get(plan, distance))
						: Vector.distance(cluster.centroid, Vector.get(plan, distance));
			cluster.averageDistance /= cluster.size;
		}
	}

	private float[] GetMean(ArrayList<RelaxedPlan> plans, Distance distance) {
		float[] mean = Vector.getEmpty(distance);
		for (RelaxedPlan plan : plans)
			mean = Vector.add(mean, Vector.get(plan, distance));
		mean = Vector.divide(mean, plans.size());
		return mean;
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
