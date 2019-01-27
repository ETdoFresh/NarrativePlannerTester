package sabre.util;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;

import sabre.Settings;

public class MutableSet<T> extends ImmutableSet<T> {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public MutableSet(T[] array) {
		super(array);
	}

	public MutableSet(Iterable<? extends T> iterable, Class<T> type) {
		super(iterable, type);
	}
	
	public MutableSet(Set<? extends T> set, Class<T> type) {
		super(set, type);
	}
	
	public void add(T element) {
		if(!contains(element))
			super.add(element);
	}
	
	public void remove(Object object) {
		super.remove(object);
	}
	
	public void apply(Function<? super T, T> function) {
		super.apply(function);
	}
	
	public void sort(Comparator<? super T> comparator) {
		super.sort(comparator);
	}
}
