package sabre.util;

import java.util.LinkedList;

import sabre.Utilities;

public class ArgumentQueue {

	private final LinkedList<String> queue;
	private String previous = null;
	
	public ArgumentQueue(String[] arguments, int start, int end) {
		queue = new LinkedList<>();
		for(int i=start; i<end; i++)
			queue.offer(arguments[i]);
	}
	
	public ArgumentQueue(String[] arguments) {
		this(arguments, 0, arguments.length);
	}
	
	public ArgumentQueue(Iterable<String> arguments) {
		this(Utilities.toArray(arguments, String.class));
	}
	
	public int size() {
		return queue.size();
	}
	
	public String peek() {
		return queue.peek();
	}
	
	public String peek(int count) {
		String string = "";
		for(int i=0; i<count; i++)
			string += queue.get(i) + " ";
		return string.trim();
	}
	
	public String pop() {
		if(size() == 0 && previous == null)
			throw new IllegalArgumentException("Expected at least one argument.");
		else if(size() == 0)
			throw new IllegalArgumentException("Expected something after \"" + previous + "\".");
		else
			return queue.poll();
	}
}
