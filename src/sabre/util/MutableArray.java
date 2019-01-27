package sabre.util;

import java.util.Comparator;
import java.util.function.Function;

import sabre.Settings;

public class MutableArray<T> extends ImmutableArray<T> {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public MutableArray(T[] array) {
		super(array);
	}
	
	public MutableArray(Iterable<? extends T> iterable, Class<T> type) {
		super(iterable, type);
	}

	public void add(T element) {
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
