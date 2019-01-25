package np;

public class Main {
	
	private static final String VERSION = "0.01";
	private static final String CREDITS = "Rachel Farrell and Edward Garcia";
	private static final String TITLE = "The Narrative Planner Tester v" + VERSION + ", by " + CREDITS;
	private static final String USAGE = "You don't know how to use this program... neither do we...";
	
	public static void main(String[] args) {
		try {
			System.out.println(TITLE);
			CommandLineArguments arguments = new CommandLineArguments(args);
			if(arguments.size() == 0 || arguments.contains("-help")) {
				System.out.println(USAGE);
				System.exit(0);
			}
			
			// Arguments
//			String loadURL = arguments.getValue("-load");
//			String domainURL = arguments.getValue("-domain");
//			String outputURL = arguments.getValue("-output");
//			String saveURL = arguments.getValue("-save");
			arguments.checkForUnusedArguments();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}