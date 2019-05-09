package qa;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import qa.Exceptions.*;
import sabre.*;
import sabre.graph.PlanGraphEventNode;
import sabre.io.DefaultParser;
import sabre.io.Parser;
import sabre.logic.Expression;
import sabre.search.Result;
import sabre.search.Search;
import sabre.space.RootNode;
import sabre.space.SearchSpace;
import sabre.state.ArrayState;

public class Main {

	private static final String VERSION = "v0.01.1";
	private static final String CREDITS = "by Edward Garcia, Rachelyn Farrell, and Stephen G. Ware";
	private static final String TITLE = "Planning Domain Automated Tester (PDAT), " + VERSION + "\n " + CREDITS + "\n";
	private static final String USAGE = "USAGE: java -jar pdat.jar <filename>\n";
	private static final String DASHLINE = "---------------------------------";
	// private static String filename = "rrh.txt";
	private static String filename = "domains/camelot.domain";

	private static final int hardCodedK = 0; // 0 for auto
	private static final int maxK = 6;
	private static final int numClustererRuns = 20;
	private static final boolean onlyExploreAuthorGoals = true;
	private static final boolean usePlanGraphExplanation = true;
	private static final DistanceMetric metric = DistanceMetric.SATSTEP_GOAL_PAIR_SCHEMAS_WEIGTHED;
	private static final boolean serializePlans = false;
	private static final boolean deserializePlans = false;
	private static final boolean dedupePlansWithActionDistance = false;
	private static final boolean dedupePlansWithMainDistance = false;

	public static final boolean isValidCheck = true;
	public static final boolean avoidAddingDuplicatesInExtractor = true;
	public static final boolean testDistances = false; // compares plan #1 to all plans including self
	public static Distance distance;
	public static final boolean considerStepsForLiteralsAlreadyTrueInInitialState = false;
	public static SearchSpace space;

	static long lastModified = 0;
	static boolean firstRun = true;
	static Result result = null;
	static Search search = null;
	static ArrayList<Plan> plans = new ArrayList<Plan>();
	static File file;

	public static void main(String[] args) throws Exception {
		if (args.length > 0)
			filename = args[0];
		else
			System.out.println(USAGE);

		printTitle();
		openDomainTxtFile();

		while (true) {
			// try { // Commented Out for debugging/stack tracing
			if (lastModified == file.lastModified()) {
				resumeSearch();
				continue;
			}
			resetLastModified();
			printLastModified();
			Domain domain = getDomain();
			space = getSearchSpace(domain);
			distance = new Distance(metric, space);
			printSpaceStatistics(space);
			checkDomainGoalEmpty(domain);
			ArrayState initial = new ArrayState(space);
			checkGoalTrueInitialState(domain, initial);
			extendPlanGraph(space, initial);
			DomainSet.Initialize(space);

			// Number of actions available from the initial state
			int firstSteps = 0;
			System.out.println(Text.INFO + "Actions possible from initial state: ");
			for (Action action : space.actions)
				if (action.precondition.test(initial)) {
					System.out.println("\t - " + action);
					firstSteps++;
				}
			System.out.println("\t (" + firstSteps + " total)");

			// Check for any unusable action schemas
			HashSet<Action> unusedActions = new HashSet<Action>();
			for (Action action : space.domain.actions) {
				boolean actionFound = false;
				for (PlanGraphEventNode graphEvent : space.graph.events)
					if (action.name == graphEvent.event.name) {
						actionFound = true;
						continue;
					}
				if (!actionFound)
					unusedActions.add(action);
			}
			if (unusedActions.size() == 0)
				System.out.println(Text.PASS + Text.ACTIONS);
			else {
				System.out.println(Text.WARN + Text.ACTIONS);
				for (Action action : unusedActions)
					System.out.println(Text.BLANK + "Unusable: " + action.toString());
				// continue;
			}

			System.out.println("\nLet's try clustering...");

			ArrayList<RelaxedPlan> relaxedPlans;
			if (deserializePlans)
				relaxedPlans = deserializeRelaxedPlans("PlanGraphExplanationsPlans");
			else
				relaxedPlans = getRelaxedPlans(space, usePlanGraphExplanation);

//			// Uncomment this to generate new comparisons.
//			Comparisons comparisons = Comparisons.compute(space, relaxedPlans);
//			comparisons.keepRandomSet(100);
			// FileIO.Write("Comparisons.json", comparisons.toString());

			System.out.println("  Total RelaxedPlans: " + relaxedPlans.size());
			System.out.println("  Valid RelaxedPlans: " + countValid(relaxedPlans, space));
			System.out.println(DASHLINE);

			if (dedupePlansWithActionDistance) {
				relaxedPlans = RelaxedPlanCleaner.deDupePlans(relaxedPlans, new Distance(DistanceMetric.ACTION, space),
						space);
				System.out.println("Plans after deduping with ACTION Distance: " + relaxedPlans.size());
			}
			if (dedupePlansWithMainDistance) {
				relaxedPlans = RelaxedPlanCleaner.deDupePlans(relaxedPlans, distance, space);
				System.out.println("Plans after deduping with " + metric + " Distance: " + relaxedPlans.size());
			}

			/**
			 * ----------------------- At this point we have all the plans
			 * ------------------------------
			 */

			if (testDistances) {
				DistanceTester tester = new DistanceTester(space);
				RelaxedPlan a = relaxedPlans.get(0);
				for (RelaxedPlan b : relaxedPlans)
					tester.testDistances(a, b);
			}

			Clusterer clusterer = null;
			Clusterer bestClusterer = clusterer;
			Clusterer[] bestClusterers = new Clusterer[maxK + 1];
			float prevMinTotalClusterDistance = 0;
			float prevSlope = -100;
			int bestK = 0;
			FileIO.Write("output.txt", "");

			// Set up k-medoids with unique RelaxedPlans
			for (int k = 1; k <= Math.min(maxK, relaxedPlans.size()); k++) {
				if (hardCodedK > 0 && hardCodedK != k)
					continue;
				Random random = new Random();

				// Run clusterer X times
				float minAverageClusterDistance = Float.POSITIVE_INFINITY;
				for (int run = 0; run < numClustererRuns; run++) {
					System.out.println("Beginning kmedoids run " + run);
					clusterer = new Clusterer(relaxedPlans, k, space.actions.size(), space, distance);

					// Randomize Cluster assignments
					for (int i = 0; i < relaxedPlans.size(); i++)
						clusterer.clusters[random.nextInt(k)].plans.add(relaxedPlans.get(i));

					if (distance.distanceMetric == DistanceMetric.SATSTEP_GOAL_PAIR_SCHEMAS_WEIGTHED
							|| distance.distanceMetric == DistanceMetric.SATSTEP_GOAL
							|| distance.distanceMetric == DistanceMetric.SATSTEP_SCHEMA_GOAL
							|| distance.distanceMetric == DistanceMetric.AUTHOR_SATSTEP_SCHEMA_GOAL)
						clusterer.kmeans();
					else
						clusterer.kmedoids();

					// Other Evaluation [Tightest Clusters]
					float averageDistanceFromCentroids = 0;
					for (RelaxedPlanCluster cluster : clusterer.clusters)
						averageDistanceFromCentroids = cluster.averageDistance;
					averageDistanceFromCentroids /= k;

					System.out.println("Done with run " + run + " with average distance from centroid: "
							+ averageDistanceFromCentroids);

					if (Float.isNaN(averageDistanceFromCentroids))
						continue;
					
					// Find the tightest clusters and store assignments
					if (minAverageClusterDistance > averageDistanceFromCentroids && !clusterer.HasEmptyCluster()) {
						minAverageClusterDistance = averageDistanceFromCentroids;
						bestClusterers[k] = clusterer.clone();

						if (k > 1) {
							float slope = minAverageClusterDistance - prevMinTotalClusterDistance;
							if ((slope <= -0.05 && prevSlope < -0.05) || hardCodedK > 0) {
								bestK = k;
								bestClusterer = clusterer.clone();
							}
						}
					}
				}

				if (k > 1)
					prevSlope = minAverageClusterDistance - prevMinTotalClusterDistance;

				System.out.println("Min Distance K = " + k + ": " + minAverageClusterDistance + " slope: " + prevSlope);
				FileIO.Append("output.txt",
						"Min Distance K = " + k + ": " + minAverageClusterDistance + " slope: " + prevSlope + "\n");
				prevMinTotalClusterDistance = minAverageClusterDistance;
			}

			// Assign best assignments to plans
			clusterer = bestClusterer;

			System.out.println(DASHLINE);
			FileIO.Append("output.txt", DASHLINE + "\n");
			// System.out.println("Final medoids: " + clusterer.toString());

			System.out.println("Selected K: " + bestK);

			System.out.println("Best clusters:\n" + clusterer.toString());
			System.out.println(DASHLINE);
			FileIO.Append("output.txt", "Selected K: " + bestK + "\n");
			FileIO.Append("output.txt", "Best clusters:\n" + clusterer.toString());
			FileIO.Append("output.txt", DASHLINE + "\n");

			// Get valid example plans based on cluster medoids
//			RelaxedPlan[] exemplars = clusterer.getExemplars();
//			System.out.println("Exemplars:");
//			for (int i = 0; i < bestK; i++)
//				System.out.println("Cluster " + i + ":\n" + exemplars[i]);
//
//			// Check if a solution exists
//			Planner planner = new Planner();
//			planner.setSearchSpace(space);
//			search = planner.getSearchFactory().makeSearch(domain.goal);
//			RootNode root = new RootNode(initial);
//			search.push(root);
//			System.out.println(Text.BLANK + "Searching for next solution...");
//			try {
//				result = runInteruptably(() -> search.getNextSolution()); // <----------------------- search
//			} catch (Exception ex) {
//				System.out.println(Text.FAIL + "Exception while searching for solution: " + ex);
//				continue;
//			}
//			if (result != null && result.plan != null)
//				System.out.println(Text.PASS + Text.SOLUTION);
//			else {
//				System.out.println(Text.FAIL + Text.SOLUTION);
//				result = null;
//				search = null;
//				continue;
//			}
			// } catch (Exception ex) {
			// System.out.println(ex);
			// continue;
			// }

		} // end while
	} // end Main

	private static int countValid(ArrayList<RelaxedPlan> plans, SearchSpace space) {
		int count = 0;
		for (RelaxedPlan plan : plans) {
			if (plan.isValid(space))
				count++;
		}
		return count;
	}

	/** Get RelaxedPlans; also write them to object files **/
	private static ArrayList<RelaxedPlan> getRelaxedPlans(SearchSpace space, boolean planGraphExp)
			throws FileNotFoundException, IOException {
		String dir;
		String txtfile;
		ArrayList<RelaxedPlan> plans;
		if (planGraphExp) {
			txtfile = "PlanGraphExplanationsPlan.txt";
			dir = "PlanGraphExplanationsPlans";
			plans = PlanGraphExplanations.getExplainedPlans(space, onlyExploreAuthorGoals); // Runs much faster!
		} else {
			txtfile = "ExplanationsPlan.txt";
			dir = "ExplanationsPlans";
			plans = RelaxedPlanExtractor.GetAllPossiblePlans(space, space.goal);
		}
		// RelaxedPlanCleaner.stopStoryAfterOneAuthorGoalComplete(space, plans);
		// RelaxedPlanCleaner.removeDuplicateSteps(plans);
		// RelaxedPlanCleaner.removeDuplicatePlans(plans);
		FileIO.Write(txtfile, plans.toString());
		if (serializePlans) {
			File file = new File(dir);
			if (!file.isDirectory())
				file.mkdir();
			int i = 0;
			for (RelaxedPlan p : plans) {
				i++;
				ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(dir + "/plan_" + i + ".ser"));
				objOut.writeObject(p);
				objOut.close();
			}
		}
		return plans;
	}

	/** Get RelaxedPlans from files **/
	private static ArrayList<RelaxedPlan> deserializeRelaxedPlans(String dir)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		ArrayList<RelaxedPlan> plans = new ArrayList<>();
		File[] planFiles = (new File(dir)).listFiles();
		for (int i = 0; i < planFiles.length; i++) {
			ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(planFiles[i]));
			plans.add((RelaxedPlan) objIn.readObject());
			objIn.close();
		}
		return plans;
	}

	private static void resumeSearch() {
		// Search in progress.....
		if (search != null && result != null) {
			// There are no more plans, finish!
			if (result.plan == null) {
				System.out.println(Text.INFO + "Search has found all plans");
				search = null;
				result = null;
				return;
			}

			// Look for next solution, stop after finding X plans
			if (plans.size() > 2) {
				System.out.println(Text.BLANK + "Cutting off after 2 solutions");
				result = null;
			} else {
				// System.out.println(Text.BLANK + "Searching for next solution...");
				result = runInteruptably(() -> search.getNextSolution());
			}

			// No Search in progress.... wait for a second and then check if file has been
			// modified
		} else {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void printTitle() {
		// Clear/Reset Screen
		System.out.flush();
		System.out.println(TITLE);
	}

	private static void openDomainTxtFile() {
		file = new File(filename);
		Desktop desktop = Desktop.getDesktop();
		try {
			file.createNewFile(); // does nothing if file exists
			desktop.open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void resetLastModified() {
		// Update last modified
		lastModified = file.lastModified();
		result = null;
		search = null;
		plans.clear();
	}

	private static void printLastModified() {
		if (firstRun) {
			firstRun = false;
			System.out.println(Text.INFO + "File Opened: " + filename + " Last Modified: "
					+ new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(lastModified));
		} else {
			System.out.println(DASHLINE + "\n");
			System.out.println(Text.INFO + "File Modified: " + filename + " Last Modified: "
					+ new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(lastModified));
		}
	}

	private static Domain getDomain() throws DomainException {
		Domain domain;
		try {
			Parser parser = new DefaultParser();
			domain = parser.parse(file, Domain.class);
		} catch (Exception ex) {
			throw new DomainException(ex.toString());
		}
		System.out.println(Text.PASS + Text.SYNTAX);
		return domain;
	}

	private static SearchSpace getSearchSpace(Domain domain) {
		return Utilities.get(status -> new SearchSpace(domain, status));
	}

	private static void printSpaceStatistics(SearchSpace space) {
		System.out.println(Text.INFO + "Number of ground actions: " + space.actions.size());
		System.out.println(Text.INFO + "Number of state variables: " + space.slots.size());
	}

	private static void checkDomainGoalEmpty(Domain domain) throws DomainEmptyException {
		if (domain.goal.equals(Expression.TRUE))
			throw new DomainEmptyException();
		else
			System.out.println(Text.PASS + Text.GOAL);
	}

	private static void checkGoalTrueInitialState(Domain domain, ArrayState initial) throws GeneralException {
		try {
			if (domain.goal.test(initial))
				throw new GoalTrueInInitialState();
			else
				System.out.println(Text.PASS + Text.INITIAL);
		} catch (Exception ex) {
			throw new GeneralException(Text.FAIL + "Exception while testing the goal in the initial state: " + ex);
		}
	}

	private static void extendPlanGraph(SearchSpace space, ArrayState initial) {
		space.graph.initialize(initial);
		while (!space.graph.hasLeveledOff())
			space.graph.extend(); // Extend graph until it levels off
	}

	public static <T> T runInteruptably(Callable<T> task) {
		T result = null;
		boolean loop = true;
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Future<T> future = executor.submit(task);
		while (loop) {
			try {
				result = future.get(1, TimeUnit.SECONDS);
				if (future.isDone()) {
					loop = false;
					result = future.get();
				}
			} catch (Exception ex) {
				if (lastModified != file.lastModified()) {
					result = null;
					break;
				}
			}
		}
		executor.shutdownNow();
		return result;
	}
}