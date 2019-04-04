package qa;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import qa.Exceptions.*;
import sabre.*;
import sabre.graph.PlanGraph;
import sabre.graph.PlanGraphEventNode;
import sabre.io.DefaultParser;
import sabre.io.Parser;
import sabre.logic.ConjunctiveClause;
import sabre.logic.Expression;
import sabre.search.Result;
import sabre.search.Search;
import sabre.space.RootNode;
import sabre.space.SearchSpace;
import sabre.state.ArrayState;

public class Main {

	private static final String VERSION = "0.01.1";
	private static final String CREDITS = "Rachel Farrell and Edward Garcia";
	private static final String TITLE = "Planning Domain Automated Tester v" + VERSION + ", by " + CREDITS
			+ "\n using the Sabre Narrative Planner v0.32 by Stephen G. Ware";
	private static final String USAGE = "... Sit back, relax, and enjoy the demo ...\n";
	private static final String FILE = "RRH.txt";

	static long lastModified = 0;
	static boolean firstRun = true;
	static Result result = null;
	static Search search = null;
	static ArrayList<Plan> plans = new ArrayList<Plan>();
	static File file;

	public static void main(String[] args) {
		printTitle();
		openDomainTxtFile();

		while (true) {
			try {
				if (lastModified == file.lastModified()) {
					resumeSearch();
					continue;
				}

				resetLastModified();
				printLastModified();

				Domain domain = getDomain();
				SearchSpace space = getSearchSpace(domain);

				printSpaceStatistics(space);
				checkDomainGoalEmpty(domain);

				ArrayState initial = new ArrayState(space);
				checkGoalTrueInitialState(domain, initial);

				PlanGraph planGraph = createExtendedPlanGraph(space, initial);
				ArrayList<RelaxedPlan> plans = getRelaxedPlans(space);

				System.out.println(Text.INFO + "Explains Domain Goal: "
						+ Explanation.IsValid(plans.get(0), domain.initial, domain.goal));
				System.out.println(Text.INFO + "Explains Reds Goal: "
						+ Explanation.IsValid(plans.get(0), domain.initial, AgentGoal.get(domain, "Red")));
				System.out.println(Text.INFO + "Explains Wolfs Goal: "
						+ Explanation.IsValid(plans.get(0), domain.initial, AgentGoal.get(domain, "Wolf")));
				System.out.println(Text.INFO + "Explains Grandmas Goal: "
						+ Explanation.IsValid(plans.get(0), domain.initial, AgentGoal.get(domain, "Grandma")));

				// TODO Comment this out later, just displaying all relaxed Solutions
				for (int i = 0; i < plans.size(); i++) {
					// System.out.println(INFO + "Relaxed Solution #" + i);
					// System.out.println(plans.get(i));
				}

				// TODO Delete this later, Plan2Vector Test
				RelaxedPlanVector rpv0 = new RelaxedPlanVector(space, plans.get(0));
				RelaxedPlanVector rpv1 = new RelaxedPlanVector(space, plans.get(2));
				System.out.println("Comparing these two relaxed plans: \n" + plans.get(0) + "\n" + plans.get(2));
				System.out.println(Text.INFO + "RPV0: " + rpv0);
				System.out.println(Text.INFO + "RPV1: " + rpv1);
				System.out.println(Text.INFO + "Intersection = " + rpv0.intersection(rpv1));
				System.out.println(Text.INFO + "Union = " + rpv0.union(rpv1));
				System.out
						.println(Text.INFO + "Action Distance = " + rpv0.intersection(rpv1) / (float) rpv0.union(rpv1));

				// ---- Clustering test ----
				RelaxedPlanVector[] planVecs = new RelaxedPlanVector[plans.size()];
				for (int i = 0; i < planVecs.length; i++) {
					planVecs[i] = new RelaxedPlanVector(space, plans.get(i));
				}
				int k = 3;
//			RelaxedPlanVector[] centroids = new RelaxedPlanVector[k];

//			System.out.println("Initializing centroids...");

				/*
				 * // First attempt at random initialization float weight =
				 * (float)planVecs[0].sum()/planVecs[0].size;
				 * System.out.println("Initializing centroids using weight: " + weight); for(int
				 * i=0; i<k; i++) centroids[i] = new RelaxedPlanVector(space, weight);
				 */

				// Trying to improve initial centroids: Set each centroid to the mean of a
				// different subset of the planVecs
				/*
				 * int segmentLength = planVecs.length / k; int startIndex = 0; for(int i=0;
				 * i<k; i++) { ArrayList<RelaxedPlanVector> segment = new ArrayList<>(); for(int
				 * j=startIndex; j-startIndex<segmentLength; j++) segment.add(planVecs[j]);
				 * centroids[i] = RelaxedPlanVector.mean(segment); startIndex += segmentLength;
				 * }
				 */
				/*
				 * System.out.println("Initial centroids: "); for(RelaxedPlanVector centroid :
				 * centroids) { System.out.println(centroid.toString() + "\n... Actions: " +
				 * centroid.getActions().toString()); }
				 */
				// Let the clustering begin
				Clusterer clusterer = new Clusterer(planVecs, k);
				Random random = new Random();
				for (int i = 0; i < planVecs.length; i++) {
					int assignment = random.nextInt(k);
					planVecs[i].clusterAssignment = assignment;
				}

				for (int i = 0; i < k; i++)
					System.out.println("Cluster " + i + " -- Initial assignments: "
							+ clusterer.getAssignments(clusterer.clusters[i].getID()).size());

				clusterer.kmeans();
				// ----------------------------

				// System.out.println(INFO + "RPV1-RPV0: " + rpv1.minus(rpv0).magnitude() + " "
				// + rpv1.minus(rpv0));
				// System.out.println();

				// TODO Delete this later, Magnitude Tester
				for (int i = 0; i < plans.size(); i++) {
					for (int j = i; j < plans.size(); j++) {
						RelaxedPlanVector vi = new RelaxedPlanVector(space, plans.get(i));
						RelaxedPlanVector vj = new RelaxedPlanVector(space, plans.get(j));
						// System.out.println("Relaxed Solution Action Distance " + i + " vs " + j + ","
						// + vi.minus(vj).magnitude());
					}
				}

				// Number of actions available from the initial state
				int firstSteps = 0;
				System.out.println(Text.INFO + "Actions possible from initial state: ");
				for (Action action : space.actions)
					if (action.precondition.test(initial)) {
						System.out.println("\t - " + action);
						firstSteps++;
					}
				System.out.println("\t (" + firstSteps + " total)");

				// TODO: Actions *motivated* from initial state, i.e. possible and the
				// characters would consent

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

				// Check if a solution exists
				Planner planner = new Planner();
				planner.setSearchSpace(space);
				search = planner.getSearchFactory().makeSearch(domain.goal);
				RootNode root = new RootNode(initial);
				search.push(root);
				System.out.println(Text.BLANK + "Searching for next solution...");
				try {
					result = runInteruptably(() -> search.getNextSolution());
				} catch (Exception ex) {
					System.out.println(Text.FAIL + "Exception while searching for solution: " + ex);
					continue;
				}
				if (result != null && result.plan != null)
					System.out.println(Text.PASS + Text.SOLUTION);
				else {
					System.out.println(Text.FAIL + Text.SOLUTION);
					result = null;
					search = null;
					continue;
				}
			} catch (Exception ex) {
				System.out.println(ex);
			}
		}
	}

	private static ArrayList<RelaxedPlan> getRelaxedPlans(SearchSpace space) {
		ArrayList<RelaxedPlan> plans = new ArrayList<>();
		for (ConjunctiveClause goal : space.goal.toDNF().arguments)
			plans.addAll(RelaxedPlanExtractor.GetAllPossiblePlanGraphPlans(space.graph, goal.arguments));
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

			int planIndex = plans.size();
			plans.add(result.plan);

			// Evaluate plan vs other plans (all plans except last plan)
			for (int i = 0; i < plans.size() - 1; i++) {
				float jaccardDistance = Jaccard.getAction(plans.get(i), result.plan);
				System.out.println(Text.BLANK + "Solution " + i + " vs Solution " + planIndex + ": " + jaccardDistance);
			}

			// Look for next solution, stop after finding X plans
			if (plans.size() > 2) {
				System.out.println(Text.BLANK + "Cutting off after 2 solutions");
				result = null;
			} else {
				System.out.println(Text.BLANK + "Searching for next solution...");
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
		System.out.println(USAGE);
	}

	private static void openDomainTxtFile() {
		file = new File(FILE);
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
			System.out.println(Text.INFO + "File Opened: " + FILE + " Last Modified: "
					+ new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(lastModified));
		} else {
			System.out.println("----------------------------------------------------------------");
			System.out.println();
			System.out.println(Text.INFO + "File Modified: " + FILE + " Last Modified: "
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

	private static PlanGraph createExtendedPlanGraph(SearchSpace space, ArrayState initial) {
		space.graph.initialize(initial);
		while (!space.graph.hasLeveledOff())
			space.graph.extend(); // Extend graph until all goals have appeared
		return space.graph;
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