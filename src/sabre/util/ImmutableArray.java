package sabre.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.RandomAccess;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import sabre.Settings;

public class ImmutableArray<T> implements CountableIterable<T>, RandomAccess, Serializable {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	protected T[] array;
	
	public ImmutableArray(T[] array) {
		this.array = array;
	}
	
	@SuppressWarnings("unchecked")
	public ImmutableArray(Iterable<? extends T> iterable, Class<T> type) {
		ArrayList<T> list = new ArrayList<>();
		for(T element : iterable)
			list.add(element);
		this.array = list.toArray((T[]) Array.newInstance(type, list.size()));
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof ImmutableArray) {
			Object[] otherArray = ((ImmutableArray<?>) other).array;
			if(array.length == otherArray.length) {
				for(int i=0; i<array.length; i++)
					if(!array[i].equals(otherArray[i]))
						return false;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public final int hashCode() {
		int hashCode = 1;
		for(int i=0; i<array.length; i++)
			hashCode = hashCode * 31 + (array[i] == null ? 0 : array[i].hashCode());
		return hashCode;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		boolean first = true;
		for(T element : array) {
			if(first)
				first = false;
			else
				sb.append(", ");
			sb.append(element);
		}
		sb.append("]");
		return sb.toString();
	}
	
	@Override
	public int size() {
		return array.length;
	}
	
	@Override
	public Iterator<T> iterator() {
		return new ArrayIterator<T>(array);
	}
	
	@Override
	public boolean contains(Object object) {
		return indexOf(object) != -1;
	}
	
	@Override
	public boolean any(Predicate<? super T> predicate) {
		for(T element : array)
			if(predicate.test(element))
				return true;
		return false;
	}
	
	@Override
	public boolean every(Predicate<? super T> predicate) {
		for(T element : array)
			if(!predicate.test(element))
				return false;
		return true;
	}
	
	@Override
	public void forEach(Consumer<? super T> consumer) {
		for(T element : array)
			consumer.accept(element);
	}
	
	public T get(int index) {
		return array[index];
	}
	
	public int indexOf(Object object) {
		for(int i=0; i<array.length; i++)
			if(array[i].equals(object))
				return i;
		return -1;
	}
	
	void add(T element) {
		array = Arrays.copyOf(array, array.length + 1);
		array[array.length - 1] = element;
	}
	
	@SuppressWarnings("unchecked")
	void remove(Object object) {
		int index = indexOf(object);
		if(index == -1)
			return;
		T[] oldArray = array;
		array = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);
		System.arraycopy(oldArray, 0, array, 0, index);
		System.arraycopy(oldArray, index + 1, array, index, array.length - index);
	}
	
	void apply(Function<? super T, T> function) {
		for(int i=0; i<array.length; i++)
			array[i] = function.apply(array[i]);
	}
	
	void sort(Comparator<? super T> comparator) {
		Arrays.sort(array, comparator);
	}
}
