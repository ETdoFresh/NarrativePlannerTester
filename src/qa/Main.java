package qa;

import java.io.File;

import sabre.*;
import sabre.io.DefaultParser;
import sabre.io.Parser;
import sabre.search.BreadthFirst;
import sabre.search.Result;
import sabre.search.Search;
import sabre.space.RootNode;
import sabre.space.SearchSpace;
import sabre.state.ArrayState;

public class Main {
	
	private static final String VERSION = "0.01.1";
	private static final String CREDITS = "Rachel Farrell and Edward Garcia";
	private static final String TITLE = "The Narrative Planner Tester v" + VERSION + ", by " + CREDITS + "\n using the Sabre Narrative Planner v0.32 by Stephen G. Ware";
	private static final String USAGE = "... Sit back, relax, and enjoy the demo ...\n";
		
	public static void main(String[] args) {
		try {
			System.out.println(TITLE);
			CommandLineArguments arguments = new CommandLineArguments(args);
						
			if(arguments.size() == 0) {
				System.out.println(USAGE);
				// Domain
				Parser parser = new DefaultParser();
				Domain domain = parser.parse(new File("domains/police.domain"), Domain.class);
				// Search Space
				SearchSpace space = Utilities.get(status -> new SearchSpace(domain, status));		
				System.out.println(space);
				// Planner
				Planner planner = new Planner();
				planner.setSearchSpace(space);
				// Search 
				planner.setSearchFactory(new BreadthFirst());
				Search search = planner.getSearchFactory().makeSearch(planner.getSearchSpace().goal);
				RootNode root = new RootNode(new ArrayState(planner.getSearchSpace()));
				search.push(root);
				Result result = Utilities.get(status -> search.getNextSolution(status));
				// Solution
				System.out.println("Result: " + result);
				if(result.plan != null)
					for(Action action : result.plan)
						System.out.println(action);
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}