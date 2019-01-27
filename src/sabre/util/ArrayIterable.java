package sabre.util;

import java.util.Iterator;
import java.util.function.Consumer;

public class ArrayIterable<T> implements Iterable<T> {

	private final T[] array;
	private final int size;
	
	public ArrayIterable(T[] array, int size) {
		this.array = array;
		this.size = size;
	}
	
	public ArrayIterable(T[] array) {
		this(array, array.length);
	}
	
	@Override
	public Iterator<T> iterator() {
		return new ArrayIterator<>(array, size);
	}
	
	@Override
	public void forEach(Consumer<? super T> consumer) {
		for(T element : array)
			consumer.accept(element);
	}
}
