package dnars.siebog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Collection;
import dnars.base.Statement;
import dnars.base.StatementParser;

public class TestUtils {
	public static void assertStatements(Collection<?> expected, Collection<Statement> actual) {
		assertEquals(expected.size(), actual.size());
		for (Object ex : expected) {
			Statement st;
			if (ex instanceof String) {
				st = StatementParser.apply((String) ex);
			} else {
				st = (Statement) ex;
			}
			assertStatement(st, actual);
		}
	}

	public static void assertStatement(Statement expected, Collection<Statement> actual) {
		for (Statement st : actual) {
			if (expected.equals(st)) {
				return;
			}
		}
		assertTrue("Statement " + expected + " not found.", false);
	}

}
