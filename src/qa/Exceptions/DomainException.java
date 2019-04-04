package qa.Exceptions;

import qa.Text;

public class DomainException extends GeneralException {
	private static final long serialVersionUID = 857943963682608797L;
	
	public DomainException(String message) {
		super(message);
	}
	
	@Override
	public String toString() {
		String str = Text.FAIL + Text.SYNTAX + "\n";
		str += Text.BLANK + message + "\n";
		return str;
	}
}