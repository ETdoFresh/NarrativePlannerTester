package sabre.space;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.function.Function;

import sabre.Entity;
import sabre.logic.Assignment;
import sabre.logic.Expression;
import sabre.logic.Logical;
import sabre.logic.SlotAssignment;
import sabre.util.ImmutableArray;
import sabre.util.Status;

final class SlotSet implements Function<Object, Object> {

	public Slot[] slots;
	private final SlotTree tree;
	private final HashMap<Slot, Slot> slotMap = new HashMap<>();
	private final HashSet<Slot> immutable = new HashSet<>();
	
	SlotSet(SlotTree tree, Status status) {
		status.setFormat("Simplifying search space: simplifying slots");
		this.slots = tree.toArray();
		this.tree = tree;
		for(int i=0; i<slots.length; i++) {
			Slot original = slots[i];
			Slot replacement = ((Slot) apply(original)).substitute(slotAssignmentsInSlots);
			if(original != replacement) {
				slots[i] = replacement;
				slotMap.put(original, replacement);
			}
		}
		makeConsistent();
	}
	
	private final Function<Object, Object> slotAssignmentsInSlots = new Function<Object, Object>() {

		@Override
		public Object apply(Object original) {
			if(original.getClass() == Assignment.class) {
				Assignment assignment = (Assignment) original;
				Slot slot = tree.get(assignment.property, assignment.arguments);
				if(!equals(assignment.arguments, slot.arguments)) {
					Slot originalSlot = slot;
					Slot replacementSlot = new Slot(slot.space, slot.id, slot.property, assignment.arguments, slot.initial);
					slotMap.put(originalSlot, replacementSlot);
					slot = replacementSlot;
				}
				return new SlotAssignment(slot, (Entity) assignment.value);
			}
			else
				return original;
		}
		
		private final boolean equals(ImmutableArray<Logical> a1, ImmutableArray<Logical> a2) {
			for(int i=0; i<a1.size(); i++)
				if(a1.get(i) != a2.get(i))
					return false;
			return true;
		}
	};
	
	public void simplify(Status status) {
		// Find immutable slots.
		status.setFormat("Simplifying search space: simplifing slots");
		ArrayList<Slot> mutable = new ArrayList<>();
		for(int i=0; i<slots.length; i++) {
			Slot slot = slots[i];
			if(isImmutable(slot))
				immutable.add(slot);
			else
				mutable.add(slot);
		}
		slots = mutable.toArray(new Slot[mutable.size()]);
		makeConsistent();
		// Remove duplicates.
		for(int i=0; i<slots.length-1; i++) {
			Slot s1 = slots[i];
			if(s1 != null) {
				for(int j=i+1; j<slots.length; j++) {
					Slot s2 = slots[j];
					if(s2 != null && equals(s1, s2)) {
						slots[j] = null;
						slotMap.put(s2, s1);
						if(!immutable.contains(s2))
							immutable.remove(s1);
					}
				}
			}
		}
		// Alphabetize slots.
		TreeSet<Slot> ordered = new TreeSet<>(ALPHABETICALLY);
		for(Slot slot : slots)
			if(slot != null)
				ordered.add(slot);
		slots = ordered.toArray(new Slot[ordered.size()]);
		// Remake slots with ordered ID numbers.
		for(int i=0; i<slots.length; i++) {
			Slot original = slots[i];
			Slot replacement = new Slot(original.space, i, original.property, original.arguments, original.initial);
			slots[i] = replacement;
			slotMap.put(original, replacement);
			if(immutable.contains(original))
				immutable.add(replacement);
		}
		// Make all references consistent.
		makeConsistent();
	}
	
	private final boolean isImmutable(Slot slot) {
		return tree.size(slot.property, slot.arguments) < 2 && !slot.arguments.any(l -> l instanceof Expression);
	}
	
	private static boolean equals(Slot s1, Slot s2) {
		if(s1.property != s2.property)
			return false;
		if(s1.arguments.size() != s2.arguments.size())
			return false;
		return s1.arguments.equals(s2.arguments);
	}
	
	private static Comparator<Slot> ALPHABETICALLY = new Comparator<Slot>() {
		@Override
		public int compare(Slot s1, Slot s2) {
			int difference = s1.property.name.compareTo(s2.property.name);
			if(difference == 0)
				difference = s1.arguments.size() - s2.arguments.size();
			for(int i=0; i<s1.arguments.size() && difference==0; i++)
				difference = s1.arguments.get(i).compareTo(s2.arguments.get(i));
			return difference;
		}
	};
	
	private void makeConsistent() {
		boolean loop = true;
		while(loop) {
			loop = false;
			for(int i=0; i<slots.length; i++) {
				Slot original = slots[i];
				Slot replacement = original.substitute(this);
				if(original != replacement) {
					slots[i] = replacement;
					slotMap.put(original, replacement);
					if(immutable.contains(original))
						immutable.add(replacement);
					loop = true;
				}
			}
		}
	}
	
	@Override
	public Object apply(Object original) {
		Object replacement = get(original);
		while(original != replacement) {
			original = replacement;
			replacement = get(replacement);
		}
		return replacement;
	}
	
	private Object get(Object original) {
		if(original instanceof Slot) {
			Slot replacement = slotMap.get((Slot) original);
			if(replacement == null)
				return original;
			else
				return replacement;
		}
		else if(original instanceof SlotAssignment) {
			SlotAssignment assignment = (SlotAssignment) original;
			if(immutable.contains(assignment.slot)) {
				if(assignment.value == assignment.slot.initial)
					return Expression.TRUE;
				else
					return Expression.FALSE;
			}
			return original;
		}
		else if(original.getClass() == Assignment.class) {
			Assignment assignment = (Assignment) original;
			Slot slot = (Slot) apply(tree.get(assignment.property, assignment.arguments));
			return new SlotAssignment(slot, (Entity) assignment.value);
		}
		else
			return original;
	}
}
