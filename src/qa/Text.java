package qa;

public class Text {

	public static final String PASS = "[" + TextColor.GREEN + "Pass" + TextColor.RESET + "] ";
	public static final String FAIL = "[" + TextColor.RED + "Fail" + TextColor.RESET + "] ";
	public static final String WARN = "[" + TextColor.YELLOW + "Warn" + TextColor.RESET + "] ";
	public static final String INFO = "[" + TextColor.BLUE + "Info" + TextColor.RESET + "] ";
	public static final String BLANK = "       ";
	
	public static final String SYNTAX = "File should be syntactically correct";
	public static final String GOAL = "Goal should be specified";
	public static final String INITIAL = "Goal should not be true in initial state";
	public static final String ACTIONS = "All action schemas should be usable";
	public static final String SOLUTION = "Goal should be achievable";

}
