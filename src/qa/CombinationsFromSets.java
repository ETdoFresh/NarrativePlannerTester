package qa;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class CombinationsFromSets<E> implements Iterator<HashSet<E>>, Iterable<HashSet<E>> {
	private int[] sizes;
	private int[] indices;
	private ArrayList<ArrayList<E>> sets;

	@SuppressWarnings("unchecked")
	public CombinationsFromSets(ArrayList<ArrayList<E>> sets) {
		this.sets = sets;

		sizes = new int[sets.size()];
		for (int i = 0; i < sizes.length; i++)
			sizes[i] = sets.get(i).size();

		indices = new int[sets.size()];
		for (int i = 0; i < indices.length; i++)
			indices[i] = 0;
		
		if (sizes[0] == 0)
			indices[0] = -1;
	}

	@Override
	public boolean hasNext() {
		return indices[0] >= 0;
	}

	@Override
	public HashSet<E> next() {
		HashSet<E> nextSet = new HashSet<E>();
		for (int i = 0; i < indices.length; i++) {
			if (sizes[i] > 0)
				nextSet.add(sets.get(i).get(indices[i]));
		}
		for (int i = indices.length - 1; i >= 0; i--) {
			indices[i]++;

			if (indices[0] == sizes[0]) // Total Termination
				indices[0] = -1;
			if (indices[i] == sizes[i]) // Loop and Increment Next Index
				indices[i] = 0;
			else // No Looping or Termination... done!
				break;
		}
		return nextSet;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not Supported!");
	}

	@Override
	public Iterator<HashSet<E>> iterator() {
		return this;
	}
}