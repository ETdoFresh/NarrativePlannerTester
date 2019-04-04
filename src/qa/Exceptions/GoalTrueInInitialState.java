package qa.Exceptions;

import qa.Text;

public class GoalTrueInInitialState extends GeneralException {
	private static final long serialVersionUID = 8355140031316107069L;

	@Override
	public String toString() {
		String str = Text.FAIL + Text.INITIAL;
		return str;
	}
}