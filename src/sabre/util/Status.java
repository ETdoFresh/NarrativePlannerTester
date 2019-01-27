package sabre.util;

public class Status {

	private Object[] template;
	
	public Status(Object...template) {
		if(template.length == 0)
			template = new Object[]{ "working..." };
		this.template = template;
	}
	
	public void setFormat(Object...template) {
		this.template = template;
	}
	
	public void update(int index, Object value) {
		if(index < 0 || index >= template.length)
			throw new IndexOutOfBoundsException("Argument " + index + " does not exist.");
		template[index] = value;
	}
	
	@Override
	public String toString() {
		Object[] template = this.template.clone();
		String string  = "";
		for(Object part : template)
			string += part.toString();
		return string;
	}
}
