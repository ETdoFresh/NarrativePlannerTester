package sabre.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;

import sabre.Settings;

public class ImmutableMap<K, V> implements Serializable {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public final K key;
	public final V value;
	public final ImmutableMap<K, V> rest;
	public final int size;
	
	public ImmutableMap(K key, V value, ImmutableMap<K, V> rest) {
		this.key = key;
		this.value = value;
		this.rest = rest;
		this.size = rest.size + 1;
	}
	
	public ImmutableMap(K key, V value) {
		this(key, value, new ImmutableMap<K, V>());
	}
	
	public ImmutableMap() {
		this.key = null;
		this.value = null;
		this.rest = null;
		this.size = 0;
	}
	
	public ImmutableMap(Map<K, V> map) {
		this(map.entrySet().iterator());
	}
	
	private ImmutableMap(Iterator<Map.Entry<K, V>> iterator) {
		if(iterator.hasNext()) {
			Map.Entry<K, V> entry = iterator.next();
			this.key = entry.getKey();
			this.value = entry.getValue();
			this.rest = new ImmutableMap<K, V>(iterator);
			this.size = rest.size + 1;
		}
		else {
			this.key = null;
			this.value = null;
			this.rest = null;
			this.size = 0;
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object other) {
		if(other instanceof ImmutableMap) {
			ImmutableMap<K, V> otherMap = (ImmutableMap<K, V>) other;
			if(size == otherMap.size) {
				for(K key : keys())
					if(!get(key).equals(otherMap.get(key)))
						return false;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public final int hashCode() {
		if(size == 0)
			return 0;
		else
			return key.hashCode() + value.hashCode() + rest.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		ImmutableMap<K, V> map = this;
		boolean first = true;
		while(map.size != 0) {
			if(first)
				first = false;
			else
				sb.append(", ");
			sb.append(key + " -> " + value);
			map = map.rest;
		}
		sb.append("]");
		return sb.toString();
	}
	
	public int size() {
		return size;
	}
	
	public void forEach(BiConsumer<K, V> consumer) {
		ImmutableMap<K, V> map = this;
		while(map.size != 0) {
			consumer.accept(map.key, map.value);
			map = map.rest;
		}
	}
	
	public CountableIterable<K> keys() {
		return new KeyIterable();
	}
	
	public CountableIterable<V> values() {
		return new ValueIterable();
	}
	
	private abstract class MapIterable<T> implements CountableIterable<T> {
		
		@Override
		public int size() {
			return ImmutableMap.this.size;
		}
	}
	
	private abstract class MapIterator<T> implements Iterator<T> {

		private ImmutableMap<K, V> map = ImmutableMap.this;
		
		@Override
		public boolean hasNext() {
			return map.size() != 0;
		}

		@Override
		public T next() {
			if(!hasNext())
				throw new NoSuchElementException("This map has no more elements.");
			T value = get(map);
			map = map.rest;
			return value;
		}
		
		protected abstract T get(ImmutableMap<K, V> map);
	}
	
	private final class KeyIterable extends MapIterable<K> {
		
		@Override
		public Iterator<K> iterator() {
			return new KeyIterator();
		}
	}
	
	private final class KeyIterator extends MapIterator<K> {

		@Override
		protected K get(ImmutableMap<K, V> map) {
			return map.key;
		}
	}
	
	private final class ValueIterable extends MapIterable<V> {
		
		@Override
		public Iterator<V> iterator() {
			return new ValueIterator();
		}
	}
	
	private final class ValueIterator extends MapIterator<V> {

		@Override
		protected V get(ImmutableMap<K, V> map) {
			return map.value;
		}
	}
	
	public V get(Object key) {
		ImmutableMap<K, V> map = this;
		while(map.size != 0) {
			if(key.equals(map.key))
				return map.value;
			map = map.rest;
		}
		return null;
	}
	
	public ImmutableMap<K, V> put(K key, V value) {
		ImmutableMap<K, V> result = remap(this, key, value);
		if(result == this)
			return new ImmutableMap<>(key, value, result);
		else
			return result;
	}
	
	private static final <K, V> ImmutableMap<K, V> remap(ImmutableMap<K, V> map, K key, V value) {
		if(map.size == 0)
			return map;
		else if(key.equals(map.key))
			return new ImmutableMap<>(key, value, map.rest);
		else {
			ImmutableMap<K, V> rest = remap(map.rest, key, value);
			if(rest == map.rest)
				return map;
			else
				return new ImmutableMap<>(map.key, map.value, rest);
		}
	}
}
