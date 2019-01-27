package sabre.graph;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;

import sabre.Settings;
import sabre.util.ArrayIterator;
import sabre.util.CountableIterable;

public class List<T> implements CountableIterable<T>, Serializable {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	private T[] array;
	private int size;
	
	@SuppressWarnings("unchecked")
	public List(Class<T> type, int capacity) {
		this.array = (T[]) Array.newInstance(type, capacity);
		this.size = 0;
	}
	
	private static final int DEFAULT_CAPACITY = 10;
	
	public List(Class<T> type) {
		this(type, DEFAULT_CAPACITY);
	}
	
	@Override
	public int size() {
		return size;
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

	public T get(int index) {
		if(index < size)
			return array[index];
		else
			throw new IndexOutOfBoundsException("Index " + index + " out of bounds.");
	}
	
	void clear() {
		size = 0;
	}
	
	void add(T element) {
		if(size == array.length)
			array = Arrays.copyOf(array, array.length * 2);
		array[size++] = element;
	}
	
	void put(int index, T element) {
		for(int i=size; i<index; i++)
			add(null);
		add(element);
	}
}
