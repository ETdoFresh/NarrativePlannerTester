package sabre.io;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Predicate;

import sabre.FormatException;
import sabre.util.CountableIterable;
import sabre.util.ImmutableArray;
import sabre.util.ImmutableList;

public class ParseTree implements CountableIterable<ParseTree> {

	private final Parser parser;
	private final ParseTree parent;
	private final ImmutableArray<ParseTree> children;
	public final ImmutableList<Token> tokens;
	public final Object type;
	private ImmutableList<Object> defined;
	
	private static final ImmutableList<Object> NONE_DEFINED = new ImmutableList<>();
		
	ParseTree(Parser parser, Iterable<ParseTree> children, ImmutableList<Token> tokens, Object type) {
		this.parser = parser;
		this.parent = null;
		this.children = new ImmutableArray<>(children, ParseTree.class);
		this.tokens = tokens;
		this.type = type;
		this.defined = NONE_DEFINED;
	}
	
	ParseTree(Parser parser,  ImmutableList<Token> tokens, Object type) {
		this(parser, new ArrayList<>(), tokens, type);
	}
	
	ParseTree(ParseTree toClone, ImmutableList<Object> defined) {
		this.parser = toClone.parser;
		this.parent = null;
		ArrayList<ParseTree> children = new ArrayList<>();
		for(ParseTree child : toClone)
			children.add(new ParseTree(this, child));
		this.children = new ImmutableArray<>(children, ParseTree.class);
		this.tokens = toClone.tokens;
		this.type = toClone.type;
		this.defined = defined;
	}
	
	private ParseTree(ParseTree parent, ParseTree toClone) {
		this.parser = toClone.parser;
		this.parent = parent;
		ArrayList<ParseTree> children = new ArrayList<>();
		for(ParseTree child : toClone)
			children.add(new ParseTree(this, child));
		this.children = new ImmutableArray<>(children, ParseTree.class);
		this.tokens = toClone.tokens;
		this.type = toClone.type;
		this.defined = toClone.defined;
	}
	
	@Override
	public String toString() {
		StringWriter string = new StringWriter();
		toString(string, 0);
		return string.toString();
	}
	
	private final void toString(StringWriter string, int indent) {
		for(int i=0; i<indent; i++)
			string.append("| ");
		if(type instanceof Class)
			string.append(((Class<?>) type).getSimpleName());
		else
			string.append(type.toString());
		string.append(" ::= ");
		string.append(Token.toString(tokens));
		string.append("\n");
		for(ParseTree child : children)
			child.toString(string, indent + 1);
	}
	
	@Override
	public Iterator<ParseTree> iterator() {
		return children.iterator();
	}

	@Override
	public int size() {
		return children.size();
	}
	
	public String getComment() {
		if(tokens.size() == 0)
			return "";
		else
			return tokens.first.comment;
	}
	
	public ParseTree child(int index) {
		return children.get(index);
	}
	
	public Object lookup(Predicate<Object> predicate) {
		for(Object object : defined)
			if(predicate.test(object))
				return object;
		if(parent == null)
			return null;
		else
			return parent.lookup(predicate);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T lookup(Class<T> type, Predicate<? super T> predicate) {
		return (T) lookup(o -> type.isAssignableFrom(o.getClass()) && predicate.test((T) o));
	}
	
	public <T> T lookup(Class<T> type) {
		return lookup(type, o -> true);
	}
	
	public void define(Object object) {
		defined = defined.add(object);
	}
	
	public <T> T build(Class<T> type) throws ParseException {
		try {
			return type.cast(getBuilder(this.type).build(this));
		}
		catch(FormatException ex) {
			throw new ParseException(ex.getMessage(), tokens);
		}
		catch(ClassCastException ex) {
			throw new ParseException("Failed to build \"" + Token.toString(tokens) + "\" as " + type.getSimpleName() + ".", tokens);
		}
	}
	
	private final Builder getBuilder(Object type) {
		if(type == Pattern.NOTHING)
			return Pattern.NOTHING_BUILDER;
		else if(type == Pattern.SYMBOL)
			return Pattern.SYMBOL_BUILDER;
		else if(type == Pattern.STRING)
			return Pattern.STRING_BUILDER;
		else if(type instanceof Keyword)
			return Keyword.KEYWORD_BUILDER;
		else if(type instanceof Sequence)
			return DEFAULT_BUILDER;
		else if(type instanceof List)
			return List.LIST_BUILDER;
		Builder builder = parser.getBuilder(this.type);		
		if(builder == null)
			builder = DEFAULT_BUILDER;
		return builder;
	}
	
	private static final Builder DEFAULT_BUILDER = new Builder() {

		@Override
		public Object build(ParseTree tree) throws ParseException {
			if(tree.size() == 0)
				return null;
			else if(tree.size() == 1)
				return tree.child(0).build();
			else
				return List.LIST_BUILDER.build(tree);
		}
	};
	
	public Object build() throws ParseException {
		return build(Object.class);
	}
}
