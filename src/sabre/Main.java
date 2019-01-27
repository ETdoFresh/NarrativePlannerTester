package sabre;

import java.io.File;
import java.util.ArrayList;

import sabre.io.DefaultParser;
import sabre.io.Parser;
import sabre.search.*;
import sabre.space.RootNode;
import sabre.space.SearchSpace;
import sabre.state.ArrayState;
import sabre.util.ArgumentQueue;
import sabre.util.CommandLineArguments;

public class Main {
	
	private static final String TITLE = "The Sabre Narrative Planner v" + Settings.VERSION_STRING + ", by " + Settings.CREDITS;
	
	private static final String USAGE = "Usage: java -jar sabre.jar [options]\n" +
		"Options include:\n" +
		"  -load <file>           Read a complete planner from file.\n" +
		"  -domain <file>         Reads a new planning domain file.\n" +
		"  -search <args>         A description of the search techniques to use.\n" +
		"     breadth             Breadth-first search.\n" +
		"     maxtime <number>    Stops search after <number> milliseconds.\n" +
		"     maxnodes <number>   Stops search after visiting <number> of nodes.\n" +
		"     maxlength <number>  Prunes plans longer than <number> steps.\n" +
		"  -save <file>           Writes the current complete planner to file.\n" +
		"  -output <file>         Writes the soultion to file.\n";
	
	public static void main(String[] args) {
		//args = new String[]{"-domain", "../../tests/police.domain", "-search", "breadth", "maxlength", "10"};
		try {
			// Title and Usage
			System.out.println(TITLE);
			CommandLineArguments arguments = new CommandLineArguments(args);
			if(arguments.size() == 0 || arguments.contains("-help")) {
				System.out.println(USAGE);
				System.exit(0);
			}
			// Arguments
			String loadURL = arguments.getValue("-load");
			String domainURL = arguments.getValue("-domain");
			ArgumentQueue searchArguments;
			if(arguments.indexOf("-search") != -1) {
				ArrayList<String> list = new ArrayList<>();
				for(int i=arguments.indexOf("-search")+1; i<args.length; i++) {
					if(args[i].startsWith("-"))
						break;
					list.add(arguments.get(i));
				}
				searchArguments = new ArgumentQueue(list);
			}
			else
				searchArguments = new ArgumentQueue(new String[0]);
			String outputURL = arguments.getValue("-output");
			String saveURL = arguments.getValue("-save");
			arguments.checkForUnusedArguments();
			// Planner
			Planner planner;
			if(loadURL == null)
				planner = new Planner();
			else
				planner = Utilities.deserialize(new File(loadURL), Planner.class);
			// Domain
			if(domainURL == null) {
				System.out.println("Domain:       " + planner.getSearchSpace().domain);
				System.out.println("Search Space: " + planner.getSearchSpace());
			}
			else {
				Parser parser = new DefaultParser();
				Domain domain = parser.parse(new File(domainURL), Domain.class);
				System.out.println("Domain:       " + domain);
				SearchSpace space = Utilities.get(status -> new SearchSpace(domain, status));
				planner.setSearchSpace(space);
				System.out.println("Search Space: " + space);
			}
			// Search Factory
			while(searchArguments.size() > 0) {
				String name = searchArguments.pop();
				switch(name) {
				case "breadth": planner.setSearchFactory(new BreadthFirst()); break;
				case "maxlength": planner.setSearchFactory(new LimitLength(planner.getSearchFactory(), searchArguments)); break;
				default: throw new IllegalArgumentException("The search type \"" + name + "\" is not defined.");
				}
			}
			System.out.println("Search:       " + planner.getSearchFactory());
			// Save
			if(saveURL != null) {
				Utilities.serialize(planner, new File(saveURL));
				System.out.println("Planner saved to file \"" + saveURL + "\".");
			}
			// Search
			else {
				Search search = planner.getSearchFactory().makeSearch(planner.getSearchSpace().goal);
				RootNode root = new RootNode(new ArrayState(planner.getSearchSpace()));
				search.push(root);
				Result result = Utilities.get(status -> search.getNextSolution(status));
				System.out.println("Result:       " + result);
				if(result.plan != null)
					for(Action action : result.plan)
						System.out.println(action);
				// Output
				if(outputURL != null) {
					System.out.println("Plan written to file \"" + outputURL + "\".");
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
