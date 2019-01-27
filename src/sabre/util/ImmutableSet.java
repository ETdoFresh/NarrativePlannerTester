package sabre.util;

import java.lang.reflect.Array;
import java.util.LinkedHashSet;
import java.util.Set;

import sabre.Settings;

public class ImmutableSet<T> extends ImmutableArray<T> {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	@SuppressWarnings("unchecked")
	public ImmutableSet(T[] array) {
		super(removeDuplicates(new ArrayIterable<T>(array), (Class<T>) array.getClass().getComponentType()));
	}
	
	public ImmutableSet(Iterable<? extends T> iterable, Class<T> type) {
		super(removeDuplicates(iterable, type));
	}
	
	@SuppressWarnings("unchecked")
	private static final <T> T[] removeDuplicates(Iterable<? extends T> iterable, Class<T> type) {
		Set<? extends T> set;
		if(iterable instanceof Set)
			set = (Set<? extends T>) iterable;
		else {
			set = new LinkedHashSet<T>();
			for(T element : iterable)
				((LinkedHashSet<T>) set).add(element);
		}
		return set.toArray((T[]) Array.newInstance(type, set.size()));
	}
	
	@SuppressWarnings("unchecked")
	public ImmutableSet(Set<T> set, Class<T> type) {
		super(set.toArray((T[]) Array.newInstance(type, set.size())));
	}
}
