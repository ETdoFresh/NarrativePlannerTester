package sabre.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayIterator<T> implements Iterator<T> {

	private final T[] array;
	private final int size;
	private int index = 0;
	
	public ArrayIterator(T[] array, int size) {
		this.array = array;
		this.size = size;
	}
	
	public ArrayIterator(T[] array) {
		this(array, array.length);
	}
	
	@Override
	public boolean hasNext() {
		return index < size;
	}

	@Override
	public T next() {
		if(hasNext())
			return array[index++];
		else
			throw new NoSuchElementException("This array has no more elements.");
	}
}
