package qa;

import java.io.File;
import java.io.IOException;

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

	public static void main(String[] args) {
		
		// Open domain file in notepad
		Runtime runtime = Runtime.getRuntime();
		try {
			Process process = runtime.exec("notepad " + FILE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		File sampleTxt = new File(FILE);
		long lastModified = 0;

		while (true) {
			if (lastModified == sampleTxt.lastModified())
				continue;

			// Update last modified
			lastModified = sampleTxt.lastModified();

			// Clear/Reset Screen  
		    System.out.flush();
			System.out.println(TITLE);
			System.out.println(USAGE);
			
			// Parse Domain
			Parser parser = new DefaultParser();
			Domain domain;
			try {
				domain = parser.parse(sampleTxt, Domain.class);
			} catch (Exception ex) {
				System.out.println(FAIL + "File is syntactically correct");
				continue;
			}
			System.out.println(PASS + "File is syntactically correct");
			
			// Check if goal state is empty
			if (domain.goal.equals(Expression.TRUE))
				System.out.println(FAIL + "Goal State specified");
			else
				System.out.println(PASS + "Goal State specified");
			
			// Check if initial state equals goal state
			if (domain.initial.equals(domain.goal))
				System.out.println(WARN + "Initial State == Goal State");
			else
				System.out.println(PASS + "Initial State == Goal State");

			// Space
			SearchSpace space = Utilities.get(status -> new SearchSpace(domain, status));
			System.out.println("Number of ground actions: " + space.actions.size());

			// Plan Graph
			space.graph.initialize(new ArrayState(space)); // Initialize graph 
			while(!space.graph.extend()) {} // Extend graph until it levels off (all goals have appeared)
			System.out.println(space.graph); // Tbc!

			// Check if there exists an action from initial state
			//Propositionalizer propositionalizer = new Propositionalizer(space, new Status());
			//MutableListState initial = new MutableListState(space);
			
			//Search search = planner.getSearchFactory().makeSearch(space.goal);

			// Planner
			Planner planner = new Planner();
			planner.setSearchSpace(space);

			
//
//			// Search
//			planner.setSearchFactory(new BreadthFirst());
//			Search search = planner.getSearchFactory().makeSearch(planner.getSearchSpace().goal);
//			RootNode root = new RootNode(new ArrayState(planner.getSearchSpace()));
//			search.push(root);
//			Result result = Utilities.get(status -> search.getNextSolution(status));
//
//			// Solution
//			System.out.println("Result: " + result);
//			if (result.plan != null)
//				for (Action action : result.plan)
//					System.out.println(action);
		}
	}
}