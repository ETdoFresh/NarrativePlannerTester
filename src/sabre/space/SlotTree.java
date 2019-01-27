package sabre.space;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import sabre.Entity;
import sabre.Property;
import sabre.Settings;
import sabre.logic.Assignment;
import sabre.logic.Expression;
import sabre.logic.Logical;
import sabre.util.ArrayIterator;

final class SlotTree implements Iterable<Slot>, Serializable {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	private final class Tree implements Serializable {

		private static final long serialVersionUID = 1L;
		
		public final Tree parent;
		public Slot slot = null;
		public Assignment assignment = null;
		public int size = 0;
		private final HashMap<Object, Tree> branches = new HashMap<>();
		
		public Tree(Tree parent) {
			this.parent = parent;
		}
		
		public Tree get(Object key) {
			if(key instanceof Expression)
				key = ((Expression) key).toDNF();
			Tree branch = branches.get(key);
			if(branch == null) {
				branch = new Tree(this);
				branches.put(key, branch);
			}
			return branch;
		}
	}
	
	private final Tree root = new Tree(null);
	private int size = 0;
	
	@Override
	public Iterator<Slot> iterator() {
		return new ArrayIterator<>(toArray());
	}
	
	private final Tree tree(Property property, Iterable<? extends Logical> arguments) {
		Tree tree = root.get(property);
		for(Logical argument : arguments)
			tree = tree.get(argument);
		return tree;
	}
	
	private final Tree tree(Property property, Iterable<? extends Logical> arguments, Entity value) {
		return tree(property, arguments).get(value);
	}
	
	public int size() {
		return size;
	}
	
	public void clear() {
		root.branches.clear();
		size = 0;
	}
	
	public int size(Property property, Iterable<? extends Logical> arguments) {
		return tree(property, arguments).size;
	}
	
	public Slot get(Property property, Iterable<? extends Logical> arguments) {
		return tree(property, arguments).slot;
	}

	public void put(Slot slot) {
		Tree tree = tree(slot.property, slot.arguments);
		if(tree.slot == null)
			size++;
		tree.slot = slot;
	}
	
	public Assignment get(Property property, Iterable<Logical> arguments, Entity value) {
		return tree(property, arguments, value).assignment;
	}
	
	public void put(Assignment assignment) {
		Tree tree = tree(assignment.property, assignment.arguments, (Entity) assignment.value);
		if(tree.assignment == null)
			tree.parent.size++;
		tree.assignment = assignment;
	}
	
	public Slot[] toArray() {
		Slot[] slots = new Slot[size];
		toArray(root, slots, 0);
		return slots;
	}
	
	private static final int toArray(Tree tree, Slot[] slots, int index) {
		if(tree.slot != null)
			slots[index++] = tree.slot;
		for(Tree branch : tree.branches.values())
			index = toArray(branch, slots, index);
		return index;
	}
}
