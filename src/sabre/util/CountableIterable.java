package sabre.util;

import java.util.function.Predicate;

public interface CountableIterable<T> extends Iterable<T> {
	
	public int size();
	
	public default boolean contains(Object object) {
		for(T element : this)
			if(object.equals(element))
				return true;
		return false;
	}
	
	public default boolean any(Predicate<? super T> predicate) {
		for(T element : this)
			if(predicate.test(element))
				return true;
		return false;
	}
	
	public default boolean every(Predicate<? super T> predicate) {
		for(T element : this)
			if(!predicate.test(element))
				return false;
		return true;
	}
}
