package sabre.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ListIterator<T> implements Iterator<T> {

	private ImmutableList<T> current;
	
	public ListIterator(ImmutableList<T> current) {
		this.current = current;
	}
	
	@Override
	public boolean hasNext() {
		return current.size != 0;
	}

	@Override
	public T next() {
		if(!hasNext())
			throw new NoSuchElementException("This list has no more elements.");
		T next = current.first;
		current = current.rest;
		return next;
	}
}
