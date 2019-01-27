package sabre;

public class FormatException extends RuntimeException {

	private static final long serialVersionUID = Settings.VERSION_UID;
	
	public FormatException(String message) {
		super(message);
	}
}
