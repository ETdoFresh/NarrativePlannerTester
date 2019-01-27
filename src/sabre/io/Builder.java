package sabre.io;

@FunctionalInterface
public interface Builder {

	public Object build(ParseTree tree) throws ParseException;
}
