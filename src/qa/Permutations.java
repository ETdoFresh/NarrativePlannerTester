package qa;

import java.util.ArrayList;
import java.util.HashSet;

public class Permutations {
	public static <T> ArrayList<ArrayList<T>> getAll(HashSet<T> goals) {
		ArrayList<T> goalList = new ArrayList<>(goals);
		ArrayList<ArrayList<T>> permutations = new ArrayList<>();
		permute(goalList, 0, goalList.size(), permutations);
		return permutations;
	}
	
	private static <T> void permute(ArrayList<T> permutation, int i, int size, ArrayList<ArrayList<T>> permutations) {
		if (i == size)
			permutations.add(new ArrayList<T>(permutation));
		else {
			for (int j = i; j < size; j++) {
				swap(permutation,i,j);
				permute(permutation, i+1, size, permutations);
				swap(permutation,i,j);
			}
		}
	}

	private static <T> void swap(ArrayList<T> array, int i, int j) {
		T temp = array.get(i);
		array.set(i, array.get(j));
		array.set(j, temp);
	}
}
