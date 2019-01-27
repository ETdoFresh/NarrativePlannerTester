package sabre.io;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import sabre.util.ImmutableList;

public class Tokenizer {
	
	private static final class Input {
		
		public int line = 1;
		public int character = 0;
		private final Reader reader;
		private int[] buffer = new int[128];
		private int size = 0;
		
		public Input(Reader reader) {
			this.reader = reader;
		}
		
		public int peek() throws IOException {
			if(size == 0) {
				buffer[0] = reader.read();
				size = 1;
			}
			return buffer[0];
		}
		
		public int pop() throws IOException {
			peek();
			int result = buffer[0];
			shiftLeft();
			if(isNewLine(result))
				line++;
			character++;
			return result;
		}
		
		public void push(int c) {
			if(isNewLine(c))
				line--;
			character--;
			shiftRight();
			buffer[0] = c;
		}
		
		private final void shiftLeft() {
			for(int i=0; i<size-1; i++)
				buffer[i] = buffer[i + 1];
			size--;
		}
		
		private final void shiftRight() {
			ensure();
			for(int i=size; i>0; i--)
				buffer[i] = buffer[i - 1];
			size++;
		}
		
		public boolean startsWith(String sequence) throws IOException {
			if(sequence == null)
				return false;
			while(size < sequence.length()) {
				ensure();
				buffer[size] = reader.read();
				size++;
			}
			for(int i=0; i<sequence.length(); i++)
				if(sequence.charAt(i) != buffer[i])
					return false;
			return true;
		}
		
		private void ensure() {
			if(buffer.length == size)
				buffer = Arrays.copyOf(buffer, buffer.length * 2);
		}
	}

	private static final boolean isEndOfStream(int input) {
		return input == -1;
	}
	
	private static final boolean isNewLine(int input) {
		return input == 10;
	}
	
	private static final boolean isWhitespace(int input) {
		return Character.isWhitespace((char) input);
	}
	
	private static final boolean isDigit(int input) {
		return Character.isDigit((char) input);
	}
	
	private static final class TokenBuilder {
		
		public int line = 1;
		public int character = 0;
		private char[] characters = new char[128];
		private int size = 0;
		
		public void push(char c) {
			if(characters.length == size)
				characters = Arrays.copyOf(characters, characters.length * 2);
			characters[size] = c;
			size++;
		}
		
		public boolean push(String special, Input input) throws IOException {
			if(special == null || !input.startsWith(special))
				return false;
			for(int i=0; i<special.length(); i++)
				push((char) input.pop());
			return true;
		}
		
		public void drain(Input input) {
			while(size > 0)
				input.push(pop());
		}
		
		public char pop() {
			size--;
			return characters[size];
		}
		
		public String getContent() {
			return new String(characters, 0, size);
		}
		
		public void clear() {
			size = 0;
		}
	}
	
	private static final Comparator<String> LONGEST_FIRST = new Comparator<String>() {

		@Override
		public int compare(String s1, String s2) {
			return s2.length() - s1.length();
		}
	};
	
	private static final String[] DEFAULT_SPECIAL = new String[]{ "{", "}", "(", ")", ",", ";", "=", ".", ":", "==", "!=", "!", "&", "|", "*" };
	
	private String lineCommentStart = null;
	private String blockCommentStart = null;
	private String blockCommentEnd = null;
	private String stringMarker = null;
	private String escapedStringMarker = null;
	private final ArrayList<String> special = new ArrayList<>();
	
	public Tokenizer(String lineCommentStart, String blockCommentStart, String blockCommentEnd, String stringMarker, String...special) {
		setLineCommentMarker(lineCommentStart);
		setBlockCommentMarkers(blockCommentStart, blockCommentEnd);
		setStringMarker(stringMarker);
		for(String string : special)
			addSpecial(string);
	}
	
	public Tokenizer() {
		this("//", "/*", "*/", "\"", DEFAULT_SPECIAL);
	}
	
	public void setLineCommentMarker(String marker) {
		this.lineCommentStart = marker;
	}
	
	public void setBlockCommentMarkers(String start, String end) {
		this.blockCommentStart = start;
		this.blockCommentEnd = end;
	}
	
	public void setStringMarker(String marker) {
		this.stringMarker = marker;
		this.escapedStringMarker = "\\" + marker;
	}
	
	public void addSpecial(String string) {
		if(special.contains(string))
			return;
		special.add(string);
		Collections.sort(special, LONGEST_FIRST);
	}
	
	public void removeSpecial(String string) {
		special.remove(string);
	}
	
	public ImmutableList<Token> tokenize(Reader reader) throws IOException {
		Input input = new Input(reader);
		TokenBuilder builder = new TokenBuilder();
		String comment = "";
		ArrayList<Token> tokens = new ArrayList<>();
		while(!isEndOfStream(input.peek())) {
			builder.clear();
			if(readToken(builder, input)) {
				String content = builder.getContent();
				if(isComment(content))
					comment += content;
				else {
					tokens.add(new Token(content.trim(), builder.line, builder.character, comment.trim()));
					comment = "";
				}
			}
		}
		return toSequence(tokens, 0);
	}
	
	private final boolean isComment(String string) {
		if(lineCommentStart != null && string.startsWith(lineCommentStart))
			return true;
		else if(blockCommentStart != null && blockCommentEnd != null && string.startsWith(blockCommentStart) && string.endsWith(blockCommentEnd))
			return true;
		else
			return false;
	}
	
	private static final ImmutableList<Token> toSequence(ArrayList<Token> tokens, int index) {
		if(index == tokens.size())
			return new ImmutableList<Token>();
		else
			return new ImmutableList<Token>(tokens.get(index), toSequence(tokens, index + 1));
	}
	
	private final boolean readToken(TokenBuilder builder, Input input) throws IOException {
		ignoreWhitespace(builder, input);
		if(isEndOfStream(input.peek()))
			return false;
		builder.line = input.line;
		builder.character = input.character;

		if(readSpecial(builder, input))
			return true;
		else if(readLineComment(builder, input))
			return true;
		else if(readBlockComment(builder, input))
			return true;
		else if(readString(builder, input))
			return true;
		else if(readNumber(builder, input))
			return true;
		while(!shouldStop(input))
			builder.push((char) input.pop());
		return true;
	}
	
	private final void ignoreWhitespace(TokenBuilder builder, Input input) throws IOException {
		int peek = input.peek();
		while(!isEndOfStream(peek) && isWhitespace(peek)) {
			input.pop();
			peek = input.peek();
		}
	}
	
	private final boolean readSpecial(TokenBuilder builder, Input input) throws IOException {
		for(String special : this.special)
			if(input.startsWith(special))
				return builder.push(special, input);
		return false;
	}
	
	private final boolean readLineComment(TokenBuilder builder, Input input) throws IOException {
		if(!builder.push(lineCommentStart, input))
			return false;
		readUntil(builder, input, i -> isNewLine(i.peek()));
		if(isNewLine(input.peek()))
			input.pop();
		builder.push('\n');
		return true;
	}
	
	private final boolean readBlockComment(TokenBuilder builder, Input input) throws IOException {
		if(!builder.push(blockCommentStart, input))
			return false;
		readUntil(builder, input, i -> i.startsWith(blockCommentEnd));
		if(input.startsWith(blockCommentEnd))
			return builder.push(blockCommentEnd, input);
		else {
			builder.drain(input);
			return false;
		}
	}
	
	private final boolean readString(TokenBuilder builder, Input input) throws IOException {
		if(!builder.push(stringMarker, input))
			return false;
		int peek = input.peek();
		while(!isEndOfStream(peek) && !input.startsWith(stringMarker)) {
			if(input.startsWith(escapedStringMarker))
				builder.push(escapedStringMarker, input);
			else {
				builder.push((char) peek);
				input.pop();
			}
			peek = input.peek();
		}
		if(input.startsWith(stringMarker))
			return builder.push(stringMarker, input);
		else {
			builder.drain(input);
			return false;
		}
	}
	
	private final boolean readNumber(TokenBuilder builder, Input input) throws IOException {
		if(builder.push("-", input))
			while(!isEndOfStream(input.peek()) && isWhitespace(input.peek()))
				builder.push((char) input.pop());
		if(!isDigit(input.peek())) {
			builder.drain(input);
			return false;
		}
		while(!isEndOfStream(input.peek()) && isDigit(input.peek()))
			builder.push((char) input.pop());
		if(builder.push(".", input)) {
			if(!isDigit(input.peek())) {
				builder.drain(input);
				return false;
			}
			while(!isEndOfStream(input.peek()) && isDigit(input.peek()))
				builder.push((char) input.pop());
		}
		if(shouldStop(input))
			return true;
		else {
			builder.drain(input);
			return false;
		}
	}
	
	@FunctionalInterface
	static interface InputPredicate {
		public boolean test(Input input) throws IOException;
	}
	
	private final void readUntil(TokenBuilder builder, Input input, InputPredicate predicate) throws IOException {
		int peek = input.peek();
		while(!isEndOfStream(peek) && !predicate.test(input)) {
			builder.push((char) peek);
			input.pop();
			peek = input.peek();
		}
	}
	
	private final boolean shouldStop(Input input) throws IOException {
		if(isEndOfStream(input.peek()))
			return true;
		else if(isWhitespace(input.peek()))
			return true;
		else if(input.startsWith(lineCommentStart))
			return true;
		else if(input.startsWith(blockCommentStart))
			return true;
		else if(input.startsWith(blockCommentEnd))
			return true;
		else if(input.startsWith(stringMarker))
			return true;
		for(String special : this.special)
			if(input.startsWith(special))
				return true;
		return false;
	}
}