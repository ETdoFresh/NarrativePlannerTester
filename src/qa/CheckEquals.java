package qa;

import sabre.logic.Assignment;
import sabre.logic.Literal;
import sabre.logic.NegatedLiteral;

public class CheckEquals {
	public static boolean Literal(Literal a, Literal b) {
		if (a.equals(b))
			return true;

		if (a instanceof NegatedLiteral) {
			NegatedLiteral nA = (NegatedLiteral)a;
			if (nA.argument.equals(b))
				return false;
			else if (nA.argument instanceof Assignment && b instanceof Assignment) {
				Assignment aNAArg = (Assignment)nA.argument;
				Assignment aB = (Assignment)b;
				if (aNAArg.property.equals(aB.property) && aNAArg.arguments.equals(aB.arguments))
					if (!aNAArg.value.equals(aB.value))
						return true;
			}
		}

		if (b instanceof NegatedLiteral) {
			NegatedLiteral nB = (NegatedLiteral)b;
			if (nB.argument.equals(a))
				return false;
			else if (nB.argument instanceof Assignment && a instanceof Assignment) {
				Assignment bNAArg = (Assignment)nB.argument;
				Assignment aA = (Assignment)a;
				if (bNAArg.property.equals(aA.property) && bNAArg.arguments.equals(aA.arguments))
					if (!bNAArg.value.equals(aA.value))
						return true;
			}
		}

		return false;
	}
}
