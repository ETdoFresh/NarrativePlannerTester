package qa.Exceptions;

public class GeneralException extends Exception {
	private static final long serialVersionUID = 3886307792680812390L;
	
	protected String message = "";

	public GeneralException() {
	}

	public GeneralException(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return message;
	}
}