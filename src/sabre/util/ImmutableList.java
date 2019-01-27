package sabre.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.function.Predicate;

import sabre.Settings;

public class ImmutableList<T> implements CountableIterable<T>, Serializable {

	private static final long serialVersionUID = Settings.VERSION_UID;

	public final T first;
	public final ImmutableList<T> rest;
	public final int size;
	
	public ImmutableList(T first, ImmutableList<T> rest) {
		this.first = first;
		this.rest = rest;
		this.size = rest.size + 1;
	}
	
	public ImmutableList(T first) {
		this(first, new ImmutableList<>());
	}
	
	public ImmutableList() {
		this.first = null;
		this.rest = null;
		this.size = 0;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof ImmutableList) {
			ImmutableList<?> otherList = (ImmutableList<?>) other;
			if(size == otherList.size) {
				ImmutableList<T> self = this;
				while(self.size != 0) {				
					if(!self.first.equals(otherList.first))
						return false;
					self = self.rest;
					otherList = otherList.rest;
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public final int hashCode() {
		if(size == 0)
			return 1;
		else if(first == null || first.hashCode() == 0)
			return rest.hashCode() * 31;
		else
			return first.hashCode() + rest.hashCode() * 31;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		boolean first = true;
		for(T element : this) {
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
		return size;
	}
	
	@Override
	public Iterator<T> iterator() {
		return new ListIterator<T>(this);
	}
	
	@Override
	public boolean contains(Object object) {
		ImmutableList<T> list = this;
		while(list.size != 0) {
			if(list.first.equals(object))
				return true;
			list = list.rest;
		}
		return false;
	}
	
	@Override
	public boolean any(Predicate<? super T> predicate) {
		ImmutableList<T> list = this;
		while(list.size != 0) {
			if(predicate.test(list.first))
				return true;
			list = list.rest;
		}
		return false;
	}
	
	@Override
	public boolean every(Predicate<? super T> predicate) {
		ImmutableList<T> list = this;
		while(list.size != 0) {
			if(!predicate.test(list.first))
				return false;
			list = list.rest;
		}
		return true;
	}
	
	public ImmutableList<T> add(T object) {
		return new ImmutableList<>(object, this);
	}
	
	public ImmutableList<T> remove(Object object) {
		return remove(this, object);
	}
	
	private static final <T> ImmutableList<T> remove(ImmutableList<T> list, Object object) {
		if(list.size() == 0)
			return list;
		else if(object.equals(list.first))
			return list.rest;
		else {
			ImmutableList<T> rest = remove(list.rest, object);
			if(rest == list.rest)
				return list;
			else
				return new ImmutableList<>(list.first, rest);
		}
	}
	
	public ImmutableList<T> removeIf(Predicate<? super T> predicate) {
		return removeIf(this, predicate);
	}
	
	private static final <T> ImmutableList<T> removeIf(ImmutableList<T> list, Predicate<? super T> predicate) {
		if(list.size() == 0)
			return list;
		else if(predicate.test(list.first))
			return removeIf(list.rest, predicate);
		else {
			ImmutableList<T> rest = remove(list.rest, predicate);
			if(rest == list.rest)
				return list;
			else
				return new ImmutableList<>(list.first, rest);
		}
	}
}
