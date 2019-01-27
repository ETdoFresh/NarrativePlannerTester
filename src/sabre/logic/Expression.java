package sabre.logic;

import java.util.function.Function;

import sabre.MutableState;
import sabre.State;

public interface Expression extends Logical {
	
	@Override
	public Expression apply(Function<Object, Object> function);
	
	public default boolean isTestable() {
		return false;
	}
	
	public default boolean test(State state) {
		throw new UnsupportedOperationException("\"" + this + "\" cannot be tested.");
	}
	
	public default boolean isImposable() {
		return false;
	}
	
	public default boolean impose(State previous, MutableState state) {
		throw new UnsupportedOperationException("\"" + this + "\" cannot be imposed.");
	}

	public Expression simplify();
	
	public Expression negate();

	public default boolean negates(Expression expression) {
		return negate().equals(expression);
	}
	
	public DNFExpression toDNF();
	
	public static final Literal TRUE = new Literal() {

		private static final long serialVersionUID = 1L;

		@Override
		public String toString() {
			return "True";
		}
		
		@Override
		public boolean isGround() {
			return true;
		}
		
		@Override
		public Literal apply(Function<Object, Object> function) {
			return this;
		}
		
		@Override
		public boolean isTestable() {
			return true;
		}

		@Override
		public boolean test(State state) {
			return true;
		}

		@Override
		public boolean isImposable() {
			return true;
		}

		@Override
		public boolean impose(State previous, MutableState state) {
			return false;
		}
		
		@Override
		public Literal simplify() {
			return this;
		}
		
		@Override
		public Literal negate() {
			return FALSE;
		}
	};
	
	public static final Literal FALSE = new Literal() {

		private static final long serialVersionUID = 1L;

		@Override
		public String toString() {
			return "False";
		}
		
		@Override
		public boolean isGround() {
			return true;
		}
		
		@Override
		public Literal apply(Function<Object, Object> function) {
			return this;
		}
		
		@Override
		public boolean isTestable() {
			return true;
		}

		@Override
		public boolean test(State state) {
			return false;
		}
		
		@Override
		public Literal simplify() {
			return this;
		}
		
		@Override
		public Literal negate() {
			return TRUE;
		}
	};
}
