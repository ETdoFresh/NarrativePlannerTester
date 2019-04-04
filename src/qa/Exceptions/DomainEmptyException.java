package qa.Exceptions;

import qa.Text;

public class DomainEmptyException extends GeneralException {
	private static final long serialVersionUID = -2013118573555526678L;

	@Override
	public String toString() {
		String str = Text.FAIL + Text.GOAL;
		return str;
	}
}