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
import java.util.HashSet;
import java.util.Random;
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
	//private static String filename = "rrh.txt";
	private static String filename = "domains/camelot.domain";

	static long lastModified = 0;
	static boolean firstRun = true;
	static Result result = null;
	static Search search = null;
	static ArrayList<Plan> plans = new ArrayList<Plan>();
	static File file;

	public static void main(String[] args) throws Exception {
		if(args.length > 0)
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
			SearchSpace space = getSearchSpace(domain);

			printSpaceStatistics(space);
			checkDomainGoalEmpty(domain);

			ArrayState initial = new ArrayState(space);
			checkGoalTrueInitialState(domain, initial);

			extendPlanGraph(space, initial);

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

			//clusterTest(space);

			System.out.println("\nLet's try clustering...");

			// Get RelaxedPlans (true = PGE, false = Explanations)
			ArrayList<RelaxedPlan> relaxedPlans = getRelaxedPlans(space, true);
			// Get RelaxedPlans from files 
			//ArrayList<RelaxedPlan> relaxedPlans = deserializeRelaxedPlans("PlanGraphExplanationsPlans");

			System.out.println("Total RelaxedPlans: " + relaxedPlans.size());
			System.out.println("Valid RelaxedPlans: " + countValid(relaxedPlans, space));

			// Remove duplicate RelaxedPlans
			ArrayList<RelaxedPlan> uniquePlans = new ArrayList<>();
			for (RelaxedPlan plan : relaxedPlans) {
				if (!uniquePlans.contains(plan))
					uniquePlans.add(plan);
			}
			
			System.out.println("Unique RelaxedPlans: " + uniquePlans.size());
			System.out.println("Unique Valid RelaxedPlans: " + countValid(uniquePlans, space));	
			System.out.println("---------------------------------");

			// Set up k-medoids with unique RelaxedPlans
			int k = 4;
			Clusterer clusterer = new Clusterer(uniquePlans.toArray(new RelaxedPlan[uniquePlans.size()]), 
				k, space.actions.size(), space, DistanceMetric.AGENT_SCHEMA);
			Random random = new Random();
			for (int i = 0; i < uniquePlans.size(); i++)
				uniquePlans.get(i).clusterAssignment = random.nextInt(k);
			for (int i = 0; i < k; i++)
				System.out.println("Cluster "+i+" has "+clusterer.getAssignments(clusterer.clusters[i].id).size()+" initial assignments.");
			System.out.println("---------------------------------");

			// Run k-medoids
			clusterer.kmedoids();
			System.out.println("---------------------------------");
			System.out.println("Final medoids:");
			for (int i=0; i<k; i++)
				System.out.println("Cluster "+i+" ("+clusterer.getAssignments(i).size()+" assignments):\n"+clusterer.clusters[i].medoid);
			System.out.println("---------------------------------");

			// Get valid example plans based on cluster medoids
			RelaxedPlan[] exemplars = clusterer.getExemplars();
			System.out.println("Exemplars:");
			for (int i=0; i<k; i++)
				System.out.println("Cluster "+i+":\n"+exemplars[i]);
			
			// Check if a solution exists
			Planner planner = new Planner();
			planner.setSearchSpace(space);
			search = planner.getSearchFactory().makeSearch(domain.goal);
			RootNode root = new RootNode(initial);
			search.push(root);
			System.out.println(Text.BLANK + "Searching for next solution...");
			try {
				result = runInteruptably(() -> search.getNextSolution()); // <---------------------------------------- search
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
			// } catch (Exception ex) {
			// System.out.println(ex);
			// continue;
			// }
		}
	}

	private static int countValid(ArrayList<RelaxedPlan> plans, SearchSpace space) {
		int count = 0;
		for (RelaxedPlan plan : plans) {
			if (plan.isValid(space))
				count++;
		}
		return count;
	}

	/** Get RelaxedPlans; also write them to object files **/
	private static ArrayList<RelaxedPlan> getRelaxedPlans(SearchSpace space, boolean planGraphExp) throws FileNotFoundException, IOException {
		String dir;
		String txtfile;
		ArrayList<RelaxedPlan> plans;
		if(planGraphExp) { 
			txtfile = "PlanGraphExplanationsPlan.txt";
			dir = "PlanGraphExplanationsPlans";
		    plans = PlanGraphExplanations.getExplainedPlans(space); // Runs much faster!
		} else {
			txtfile = "ExplanationsPlan.txt";
			dir = "ExplanationsPlans";
			plans = RelaxedPlanExtractor.GetAllPossiblePlans(space, space.goal);
		}
		RelaxedPlanCleaner.stopStoryAfterOneAuthorGoalComplete(space, plans);
		RelaxedPlanCleaner.removeDuplicateSteps(plans);
		RelaxedPlanCleaner.removeDuplicatePlans(plans);
		FileIO.Write(txtfile, plans.toString());
		File file = new File(dir);
		if(!file.isDirectory())
			file.mkdir();
		// Commenting for now to speed up computations... will put in back once we ready to serialize
//		int i=0;
//		for(RelaxedPlan p : plans) {
//			i++;
//			ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(dir + "/plan_" + i + ".ser"));
//			objOut.writeObject(p);
//			objOut.close();
//		}		
		return plans;
	}
	
	/** Get RelaxedPlans from files **/
	private static ArrayList<RelaxedPlan> deserializeRelaxedPlans(String dir) throws FileNotFoundException, IOException, ClassNotFoundException{
		ArrayList<RelaxedPlan> plans = new ArrayList<>();
		File[] planFiles = (new File(dir)).listFiles();
		for(int i=0; i<planFiles.length; i++) {
			ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(planFiles[i]));
			plans.add((RelaxedPlan)objIn.readObject());
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
				//System.out.println(Text.BLANK + "Searching for next solution...");
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
			System.out.println("----------------------------------------------------------------");
			System.out.println();
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