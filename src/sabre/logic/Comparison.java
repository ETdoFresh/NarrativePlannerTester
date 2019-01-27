package sabre.logic;

import java.io.Serializable;
import java.util.function.Function;

import sabre.Settings;
import sabre.State;

public class Comparison implements Literal {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public interface Operator extends Serializable {
		
		public boolean compare(Logical left, Logical right);
		
		public Operator negate();
	}
	
	public static final Operator EQUALS = new Operator() {

		private static final long serialVersionUID = 1L;
		
		@Override
		public String toString() {
			return "==";
		}
		
		@Override
		public boolean compare(Logical left, Logical right) {
			return left.equals(right);
		}

		@Override
		public Operator negate() {
			return Comparison.NOT_EQUALS;
		}
	};
	
	public static final Operator NOT_EQUALS = new Operator() {

		private static final long serialVersionUID = 1L;
		
		@Override
		public String toString() {
			return "!=";
		}
		
		@Override
		public boolean compare(Logical left, Logical right) {
			return !left.equals(right);
		}

		@Override
		public Operator negate() {
			return Comparison.EQUALS;
		}
	};
	
	public final Operator operator;
	public final Logical left;
	public final Logical right;
	private final int hashCode;
	
	public Comparison(Operator operator, Logical left, Logical right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
		this.hashCode = left.hashCode() + right.hashCode() * 31 + operator.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Comparison) {
			Comparison otherComparison = (Comparison) other;
			return operator == otherComparison.operator &&
				((left.equals(otherComparison.left) && right.equals(otherComparison.right)) ||
				 (left.equals(otherComparison.right) && right.equals(otherComparison.left)));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public String toString() {
		return left + " " + operator + " " + right;
	}

	@Override
	public boolean isGround() {
		return left.isGround() && right.isGround();
	}
	
	@Override
	public Comparison apply(Function<Object, Object> function) {
		Operator operator = (Operator) function.apply(this.operator);
		Logical left = (Logical) function.apply(this.left);
		Logical right = (Logical) function.apply(this.right);
		if(operator != this.operator || left != this.left || right != this.right)
			return new Comparison(operator, left, right);
		else
			return this;
	}
	
	@Override
	public boolean isTestable() {
		return true;
	}
	
	@Override
	public boolean test(State state) {
		return operator.compare(left, right);
	}
	
	@Override
	public Expression simplify() {
		if(isGround()) {
			if(operator.compare(left, right))
				return Expression.TRUE;
			else
				return Expression.FALSE;
		}
		else
			return this;
	}
	
	@Override
	public Comparison negate() {
		return new Comparison(operator.negate(), left, right);
	}
}
