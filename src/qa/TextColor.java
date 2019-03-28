package qa;

public final class TextColor {
	
	private static final boolean SHOW_COLOR = false
			;
	public static final String RESET = SHOW_COLOR ? "\u001B[0m" : "";
	
	public static final String BLACK = SHOW_COLOR ? "\u001B[30m" : "";
	public static final String RED = SHOW_COLOR ? "\u001B[1;31m" : "";
	public static final String GREEN = SHOW_COLOR ? "\u001B[32m" : "";
	public static final String YELLOW = SHOW_COLOR ? "\u001B[33m" : "";
	public static final String BLUE = SHOW_COLOR ? "\u001B[34m" : "";
	public static final String PURPLE = SHOW_COLOR ? "\u001B[35m" : "";
	public static final String CYAN = SHOW_COLOR ? "\u001B[36m" : "";
	public static final String WHITE = SHOW_COLOR ? "\u001B[37m" : "";
	
	public static final String BLACK_BACKGROUND = SHOW_COLOR ? "\u001B[40m" : "";
	public static final String RED_BACKGROUND = SHOW_COLOR ? "\u001B[41m" : "";
	public static final String GREEN_BACKGROUND = SHOW_COLOR ? "\u001B[42m" : "";
	public static final String YELLOW_BACKGROUND = SHOW_COLOR ? "\u001B[43m" : "";
	public static final String BLUE_BACKGROUND = SHOW_COLOR ? "\u001B[44m" : "";
	public static final String PURPLE_BACKGROUND = SHOW_COLOR ? "\u001B[45m" : "";
	public static final String CYAN_BACKGROUND = SHOW_COLOR ? "\u001B[46m" : "";
	public static final String WHITE_BACKGROUND = SHOW_COLOR ? "\u001B[47m" : "";
}