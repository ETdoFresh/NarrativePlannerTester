package sabre;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import sabre.logic.Term;
import sabre.logic.TermVariable;
import sabre.space.Slot;
import sabre.util.Status;

public class Utilities {
	
	public static final Comparator<Unique> SORT_BY_ID = new Comparator<Unique>() {

		@Override
		public int compare(Unique u1, Unique u2) {
			return u1.id - u2.id;
		}
	};
	
	public static final int size(Iterable<?> iterable) {
		int size = 0;
		for(Iterator<?> iterator = iterable.iterator(); iterator.hasNext();)
			size++;
		return size;
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T[] toArray(Iterable<T> iterable, Class<T> type) {
		ArrayList<T> list = new ArrayList<>();
		for(T element : iterable)
			list.add(element);
		return list.toArray((T[]) Array.newInstance(type, list.size()));
	}
	
	public static final boolean equals(Iterable<?> i1, Iterable<?> i2) {
		Iterator<?> it1 = i1.iterator();
		Iterator<?> it2 = i2.iterator();
		while(it1.hasNext() && it2.hasNext())
			if(!it1.next().equals(it2.next()))
				return false;
		return it1.hasNext() == it2.hasNext();
	}

	public static final boolean isNull(Term term) {
		return term instanceof Entity && ((Entity) term).id == Settings.EMPTY_ENTITY_ID;
	}
	
	public static final Entity getNull(Domain domain) {
		return domain.entities.get(Settings.EMPTY_ENTITY_ID);
	}
	
	public static final Entity getFalse(Domain domain) {
		return getNull(domain);
	}
	
	public static final Entity getTrue(Domain domain) {
		return domain.entities.get(Settings.BOOLEAN_TRUE_ID);
	}
	
	public static final boolean isBoolean(Term term) {
		Domain domain;
		if(term instanceof Entity)
			domain = ((Entity) term).domain;
		else
			domain = ((TermVariable) term).type.domain;
		return term.is(domain.types.get(Settings.BOOLEAN_TYPE_ID));
	}
	
	public static final boolean isBoolean(Property property) {
		return property.type.is(property.domain.types.get(Settings.BOOLEAN_TYPE_ID));
	}
	
	public static final boolean isGoal(Slot slot) {
		return slot.property.id == Settings.INTENTIONAL_PROPERTY_ID;
	}
	
	public static final boolean isAgent(Term term) {
		if(term instanceof Agent)
			return true;
		else if(term instanceof TermVariable) {
			Domain domain = ((TermVariable) term).type.domain;
			Type agentType = domain.types.get(Settings.AGENT_TYPE_ID);
			return term.is(agentType);
		}
		else
			return false;
	}
	
	public static final String capitalize(String string) {
		if(string.length() == 0)
			return string;
		else
			return string.substring(0, 1).toUpperCase() + string.substring(1);
	}
	
	public static final String toFunctionString(Object name, Iterable<?> arguments) {
		String string = name.toString() + "(";
		boolean first = true;
		for(Object argument : arguments) {
			if(first)
				first = false;
			else
				string += ", ";
			string += argument;
		}
		return string + ")";
	}
	
	public static final String toDisjunctionString(Object[] objects) {
		String string = "";
		for(int i=0; i<objects.length; i++) {
			string += "\"" + objects[i] + "\"";				
			if(i == objects.length - 1)
				break;
			if(objects.length > 2)
				string += ",";
			if(i == objects.length - 2)
				string += " or ";
			else
				string += " ";
		}
		return string;
	}
	
	public static final String toDisjunctionString(Iterable<?> objects) {
		ArrayList<Object> list = new ArrayList<>();
		for(Object object : objects)
			list.add(object);
		return toDisjunctionString(list.toArray(new Object[list.size()]));
	}
	
	@FunctionalInterface
	public static interface Task<T> {
		
		public T run(Status status) throws Exception;
	}
	
	public static <T> T get(Task<T> task) {
		T result = null;
		Status status = new Status();
		int length = -1;
		ExecutorService executor = Executors.newFixedThreadPool(1);
		try {
			Future<T> future = executor.submit(() -> task.run(status));
			boolean loop = true;
			while(loop) {
				try {
					result = future.get(1, TimeUnit.SECONDS);
					if(future.isDone()) {
						loop = false;
						result = future.get();
					}
				}
				catch(ExecutionException ex) {
					if(ex.getCause() instanceof RuntimeException)
						throw (RuntimeException) ex.getCause();
					else
						throw new RuntimeException(ex.getCause());
				}
				catch(Exception ex) {
					/* do nothing */
				}
				if(result == null) {
					if(length != -1)
						System.out.print("\r");
					String string = pad(status.toString(), length);
					System.out.print(string);
					length = Math.max(length, string.length());
				}
			}
		}
		finally {
			executor.shutdown();
		}
		if(length != -1)
			System.out.print("\r" + pad("", length) + "\r");
		return result;
	}
	
	private static final String pad(String string, int length) {
		StringWriter writer = new StringWriter();
		writer.append(string);
		for(int i=0; i<(length-string.length()); i++)
			writer.append(" ");
		return writer.toString();
	}
	
	public static final void serialize(Serializable object, File file) throws IOException {
		try(ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
			out.writeObject(object);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T deserialize(File file, Class<T> type) throws IOException, ClassNotFoundException {
		try(ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			return (T) in.readObject();
		}
	}
}
