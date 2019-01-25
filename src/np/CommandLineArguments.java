package np;

public class CommandLineArguments {

	private final String[] arguments;
	private final boolean[] used;
	
	public CommandLineArguments(String[] args) {
		this.arguments = args;
		this.used = new boolean[args.length];
	}
	
	public int size() {
		return arguments.length;
	}
	
	public String get(int index) {
		used[index] = true;
		return arguments[index];
	}
	
	public int indexOf(String argument) {
		for(int i=0; i<arguments.length; i++) {
			if(arguments[i].equals(argument)) {
				used[i] = true;
				return i;
			}
		}
		return -1;
	}
	
	public boolean contains(String argument) {
		for(int i=0; i<arguments.length; i++) {
			if(arguments[i].equals(argument)) {
				used[i] = true;
				return true;
			}
		}
		return false;
	}
	
	public String getValue(String key) {
		for(int i=0; i<arguments.length; i++) {
			if(arguments[i].equals(key)) {
				if(i == arguments.length - 1)
					throw new IllegalArgumentException("Expected something after \"" + key + "\".");
				used[i] = true;
				used[i + 1] = true;
				return arguments[i + 1];
			}
		}
		return null;
	}
	
	public void checkForUnusedArguments() {
		for(int i=0; i<arguments.length; i++)
			if(!used[i])
				throw new IllegalArgumentException("Unrecognized argument \"" + arguments[i] + "\".");
	}
}
