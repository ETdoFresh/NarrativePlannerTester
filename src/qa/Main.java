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

	private static final String VERSION = "v0.01.1";
	private static final String CREDITS = "by Edward Garcia, Rachelyn Farrell, and Stephen G. Ware";
	private static final String TITLE = "Planning Domain Automated Tester (PDAT), " + VERSION + "\n " + CREDITS + "\n";
	private static final String USAGE = "TODO: Write usage";
	private static final String FILE = "rrh.txt";

	static long lastModified = 0;
	static boolean firstRun = true;
	static Result result = null;
	static Search search = null;
	static ArrayList<Plan> plans = new ArrayList<Plan>();
	static File file;

	public static void main(String[] args) throws Exception {
		printTitle();
		openDomainTxtFile();

		while (true) {
			//try { // Commented Out for debugging/stack tracing
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
				
				ArrayList<RelaxedPlan> classicalPlan = RelaxedPlanExtractor.GetAllPossibleClassicalPlans(space, space.goal);
				RelaxedPlanCleaner.RemoveNoOps(classicalPlan);
				RelaxedPlanCleaner.RemoveDuplicates(classicalPlan);
				ArrayList<RelaxedPlan> usingExplanations = getRelaxedPlans(space);
				ArrayList<RelaxedPlan> plans = PlanGraphExplanations.getExplainedPlans(space);
				RelaxedPlanCleaner.RemoveNoOps(plans);
				RelaxedPlanCleaner.RemoveDuplicates(plans);
				
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

				clusterTest(space);

				// Check if a solution exists
				Planner planner = new Planner();
				planner.setSearchSpace(space);
				search = planner.getSearchFactory().makeSearch(domain.goal);
				RootNode root = new RootNode(initial);
				search.push(root);
				System.out.println(Text.BLANK + "Searching for next solution...");
				try {
					//result = runInteruptably(() -> search.getNextSolution()); // <---------------------- search
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
			//} catch (Exception ex) {
			//	System.out.println(ex);
			//	continue;
			//}
		}
	}
	
	private static void clusterTest(SearchSpace space) {
		System.out.println("\nLet's try clustering...");
		ArrayList<RelaxedPlan> relaxedPlans = getRelaxedPlans(space);
		ArrayList<RelaxedPlan> validPlans = new ArrayList<>();
		for(RelaxedPlan plan : relaxedPlans) {	
			System.out.println("Important: " + plan.getImportantSteps(space));
			if(plan.isValid(space))
				validPlans.add(plan);
		}

		RelaxedPlanVector[] planVecs = new RelaxedPlanVector[relaxedPlans.size()];
		for(int i=0; i<planVecs.length; i++)
			planVecs[i] = new RelaxedPlanVector(space, relaxedPlans.get(i));				
		ArrayList<RelaxedPlanVector> uniquePlanVecs = new ArrayList<>();
		for(RelaxedPlanVector vec : planVecs) {
			if(!uniquePlanVecs.contains(vec))
				uniquePlanVecs.add(vec);
		}
		ArrayList<RelaxedPlan> uniquePlans = new ArrayList<>();
		for(RelaxedPlan plan : relaxedPlans) {
			if(!uniquePlans.contains(plan))
				uniquePlans.add(plan);
		}
		System.out.println("Total plans: " + relaxedPlans.size());
		System.out.println("Valid plans: " + validPlans.size());
		System.out.println("Unique plans: " + uniquePlans.size());
		System.out.println("Unique plan vectors: " + uniquePlanVecs.size());

		int k=3;

/*
		System.out.println("TEST K-MEDOIDS USING VECTORS");
		Clusterer clusterer = new Clusterer(uniquePlanVecs.toArray(new RelaxedPlanVector[uniquePlanVecs.size()]), k, space.actions.size());
		Random random = new Random();
		for(int i=0; i<uniquePlanVecs.size(); i++) 
			uniquePlanVecs.get(i).clusterAssignment = random.nextInt(k);			
		for(int i=0; i<k; i++)
			System.out.println("Cluster "+i+ " has " + clusterer.getVectorAssignments(clusterer.clusters[i].id).size() + " initial assignments.");
		clusterer.kmedoids();
		System.out.println("---------------------------------");
		System.out.println("Final centroids:");
		for(int i=0; i<k; i++)
			System.out.println(i + ": " + clusterer.clusters[i].centroid + "\nAssignments: " + clusterer.getVectorAssignments(i).size());
		System.out.println("---------------------------------");
*/		
		// Test k-medoids without vectors
		System.out.println("\nTEST K-MEDOIDS (WITHOUT VECTORS)\n");
		Clusterer clusterer = new Clusterer(uniquePlans.toArray(new RelaxedPlan[uniquePlans.size()]), k, space.actions.size(), space);
		Random random = new Random();
		for(int i=0; i<uniquePlans.size(); i++) 
			uniquePlans.get(i).clusterAssignment = random.nextInt(k);
		for(int i=0; i<k; i++)
			System.out.println("Cluster "+i+ " has " + clusterer.getPlanAssignments(clusterer.clusters[i].id).size() + " initial assignments.");
		clusterer.kmedoids(false);
		System.out.println("---------------------------------");
		System.out.println("Final medoids:");
		for(int i=0; i<k; i++)
			System.out.println("Cluster " + i + " (" + clusterer.getPlanAssignments(i).size() + " assignments):\n" + clusterer.clusters[i].medoid);
		System.out.println("---------------------------------");
		
		RelaxedPlan[] exemplars = clusterer.getExemplars();
		System.out.println("Exempars:");
		for(int i=0; i<k; i++) {
			System.out.println(i + ": " + exemplars[i]);
		}
		
		// ----------------------------

	}

	private static ArrayList<RelaxedPlan> getRelaxedPlans(SearchSpace space) {
		ArrayList<RelaxedPlan> plans = RelaxedPlanExtractor.GetAllPossiblePlans(space, space.goal);
		
		// Remove NoOps
		for (RelaxedPlan plan : plans)
			plan.removeNoOps();
		
		// Deduplicate Plans
		for (int i = plans.size() -1; i >= 0; i--)
			for (int j = i - 1; j >= 0; j--)
				if (plans.get(i).equals(plans.get(j))) {
					plans.remove(i);
					break;
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