package qa;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import sabre.*;
import sabre.graph.PlanGraph;
import sabre.io.DefaultParser;
import sabre.io.Parser;
import sabre.logic.BooleanExpression;
import sabre.logic.Expression;
import sabre.search.BreadthFirst;
import sabre.search.Result;
import sabre.search.Search;
import sabre.space.RootNode;
import sabre.space.SearchSpace;
import sabre.state.ArrayState;
import sabre.state.ListState;
import sabre.state.MutableListState;

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

	public static void main(String[] args) {

		File file = new File(FILE);

		// Open domain file
		Desktop desktop = Desktop.getDesktop();
		try {
			file.createNewFile(); // does nothing if file exists
			desktop.open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		long lastModified = 0;

		// Clear/Reset Screen
		System.out.flush();
		System.out.println(TITLE);
		System.out.println(USAGE);

		boolean firstRun = true;
		while (true) {
			if (lastModified == file.lastModified()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}

			// Update last modified
			lastModified = file.lastModified();

			if (firstRun) {
				firstRun = false;
				System.out.println(INFO + "File Opened: " + FILE + " Last Modified: " + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(lastModified));
			} else {
				System.out.println();
				System.out.println();
				System.out.println(INFO + "File Modified: " + FILE + " Last Modified: " + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(lastModified));
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
				System.out.println(FAIL + GOAL);
			else
				System.out.println(PASS + GOAL);

			// Check if initial state equals goal state <-- doesn't make sense bc goal is
			// not a state
//			if (domain.initial.equals(domain.goal))
//				System.out.println(WARN + "Initial State == Goal State");
//			else
//				System.out.println(PASS + "Initial State == Goal State");

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
			Search search = planner.getSearchFactory().makeSearch(domain.goal);
			RootNode root = new RootNode(initial);
			search.push(root);
			Result result = Utilities.get(status -> search.getNextSolution(status));
			if (result.plan != null)
				System.out.println(PASS + SOLUTION);
			else
				System.out.println(FAIL + SOLUTION);

			// Find all solutions
			/*
			 * while(result.plan != null) { System.out.println(result); for (Action action :
			 * result.plan) System.out.println(action); result = Utilities.get(status ->
			 * search.getNextSolution(status)); }
			 */
		}
	}
}