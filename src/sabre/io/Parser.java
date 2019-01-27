package sabre.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import sabre.util.ImmutableList;

public class Parser {

	private Tokenizer tokenizer = new Tokenizer();
	final HashMap<String, String> brackets = new HashMap<>();
	final ArrayList<Rule> rules = new ArrayList<>();
	private final HashMap<Object, Builder> builders = new HashMap<>();
	private ImmutableList<Object> defined = new ImmutableList<>();
	
	public Tokenizer getTokenizer() {
		return tokenizer;
	}
	
	public void setTokenizer(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}
	
	public String getRightBracket(String left) {
		return brackets.get(left);
	}
	
	public void setBrackets(String left, String right) {
		brackets.put(left, right);
	}
	
	public void clearBrackets() {
		brackets.clear();
	}
	
	public Iterable<Rule> rules() {
		return rules;
	}
	
	public void addRule(Rule rule) {
		rules.add(rule);
	}
	
	public void addRule(Object left, Object...right) {
		addRule(new Rule(left, right));
	}
	
	public void removeRule(Rule rule) {
		rules.remove(rule);
	}
	
	public void clearRules() {
		rules.clear();
	}
	
	public Builder getBuilder(Object key) {
		return builders.get(NonTerminal.key(key));
	}
	
	public void setBuilder(Object key, Builder builder) {
		builders.put(NonTerminal.key(key), builder);
	}
	
	public void clearBuilders() {
		builders.clear();
	}
	
	public void define(Object object) {
		defined = defined.add(object);
	}
	
	public void clearDefined() {
		defined = new ImmutableList<>();
	}
	
	public <T> T parse(ImmutableList<Token> tokens, Class<T> type) throws ParseException {
		Object best = null;
		for(Rule rule : rules) {
			try {
				Result result = rule.apply(this, new ImmutableList<Path>(new Path(new NonTerminal(rule.left), tokens)), tokens);
				if(result.remainder.size() == 0)
					return new ParseTree(result.tree, defined).build(type);
				else
					best = Token.compare(best, result);
			}
			catch(ParseException ex) {
				best = Token.compare(best, ex);
			}
		}
		if(best == null)
			throw new ParseException("Failed to parse " + type + ".", tokens);
		else if(best instanceof Result)
			throw ((Result) best).exception;
		else
			throw (ParseException) best;
	}
	
	public <T> T parse(Reader reader, Class<T> type) throws IOException, ParseException {
		return parse(tokenizer.tokenize(reader), type);
	}
	
	public <T> T parse(String string, Class<T> type) throws ParseException {
		try {
			return parse(new StringReader(string), type);
		}
		catch(IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public <T> T parse(File file, Class<T> type) throws IOException, ParseException {
		return parse(new BufferedReader(new FileReader(file)), type);
	}
}
