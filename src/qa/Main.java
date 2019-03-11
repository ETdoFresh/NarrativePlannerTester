package qa;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import sabre.*;
import sabre.io.DefaultParser;
import sabre.io.Parser;
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
	private static final String FILE = "sample.txt";

	private static final String PASS = "[" + TextColor.GREEN + "Pass" + TextColor.RESET + "] ";
	private static final String FAIL = "[" + TextColor.RED + "Fail" + TextColor.RESET + "] ";
	private static final String WARN = "[" + TextColor.YELLOW + "Warn" + TextColor.RESET + "] ";
	private static final String INFO = "[" + TextColor.BLUE + "Info" + TextColor.RESET + "] ";

	private static final String SYNTAX = "File is syntactically correct";
	private static final String GOAL = "Goal specified";
	private static final String INITIAL = "Goal not true in initial state";
	private static final String SOLUTION = "Solution exists";
	
	static long lastModified = 0;
	static boolean firstRun = true;
	static Result result = null;
	static Search search = null;
	static ArrayList<Plan> plans = new ArrayList<Plan>();

	public static void main(String[] args) {

		// Open text file on desktop
		File file = new File(FILE);
		Desktop desktop = Desktop.getDesktop();
		try {
			file.createNewFile(); // does nothing if file exists
			desktop.open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Clear/Reset Screen
		System.out.flush();
		System.out.println(TITLE);
		System.out.println(USAGE);

		while (true) {
			if (lastModified == file.lastModified()) {
				if (search != null && result != null) {
					if (result.plan == null) {
						System.out.println(INFO + "Search has found all plans");
						search = null;
						result = null;
						continue;
					}
					
					int planIndex = plans.size();
					System.out.println(INFO + "---------------- Plan " + planIndex + " ----------------");
					System.out.println(INFO + result);
					for (Action action : result.plan)
						System.out.println(INFO + action);
					
					plans.add(result.plan);
					
					// Evaluate plan vs other plans (all plans except last plan)
					for (int i = 0; i < plans.size() - 1; i++)
					{
						float jaccardDistance = getActionJaccard(plans.get(i), result.plan);
						System.out.println(INFO + "Plan " + i + " vs Plan " + planIndex + ": " + jaccardDistance);
					}
					
					// Find next solution
					System.out.println(INFO + "Searching for next solution...");
					result = search.getNextSolution();
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				continue;
			}

			// Update last modified
			lastModified = file.lastModified();
			result = null;
			search = null;
			plans.clear();

			if (firstRun) {
				firstRun = false;
				System.out.println(INFO + "File Opened: " + FILE + " Last Modified: "
						+ new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(lastModified));
			} else {
				System.out.println("----------------------------------------------------------------");
				System.out.println();
				System.out.println(INFO + "File Modified: " + FILE + " Last Modified: "
						+ new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(lastModified));
			}

			// Parse Domain
			Parser parser = new DefaultParser();
			Domain domain;
			try {
				domain = parser.parse(file, Domain.class);
			} catch (Exception ex) {
				System.out.println(FAIL + SYNTAX);
				continue;
			}
			System.out.println(PASS + SYNTAX);

			// Space
			SearchSpace space = Utilities.get(status -> new SearchSpace(domain, status));
			System.out.println(INFO + "Number of ground actions: " + space.actions.size());

			// Check if goal is empty
			if (domain.goal.equals(Expression.TRUE))
				System.out.println(WARN + GOAL);
			else
				System.out.println(PASS + GOAL);

			// Check if goal is true in initial state
			ArrayState initial = new ArrayState(space);
			if (domain.goal.test(initial))
				System.out.println(FAIL + INITIAL);
			else
				System.out.println(PASS + INITIAL);

			// Plan Graph
			space.graph.initialize(initial);
			while (!space.graph.hasLeveledOff())
				space.graph.extend(); // Extend graph until all goals have appeared
			System.out.println(INFO + "Size of plan graph: " + space.graph.size());

			// Number of actions available from the initial state
			int firstSteps = 0;
			for (Action action : space.actions)
				if (action.precondition.test(initial))
					firstSteps++;
			System.out.println(INFO + "Number of actions available in initial state: " + firstSteps);

			// Check if a solution exists
			Planner planner = new Planner();
			planner.setSearchSpace(space);
			search = planner.getSearchFactory().makeSearch(domain.goal);
			RootNode root = new RootNode(initial);
			search.push(root);
			System.out.println(INFO + "Searching for next solution...");
			result = search.getNextSolution();
			if (result.plan != null)
				System.out.println(PASS + SOLUTION);
			else
			{
				System.out.println(FAIL + SOLUTION);
				result = null;
				search = null;
			}
		}
	}
	
	private static <E> float getJaccard(Set<E> a, Set<E> b) {	
		HashSet<E> intersection = new HashSet<>();
		HashSet<E> union = new HashSet<>();
		union.addAll(a);
		union.addAll(b);
		for(E item : a)
			if(b.contains(item))
				intersection.add(item);
		return 1 - (float)intersection.size() / union.size();
	}
	
	private static float getActionJaccard(Plan a, Plan b)
	{
		HashSet<Action> set_a = new HashSet<>();
		HashSet<Action> set_b = new HashSet<>();
		
		for (Action action : a)
			set_a.add(action);
		
		for (Action action : b)
			set_b.add(action);
		
		return getJaccard(set_a, set_b);
	}
}