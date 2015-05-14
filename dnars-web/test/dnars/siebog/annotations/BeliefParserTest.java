package dnars.siebog.annotations;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import dnars.base.Statement;
import dnars.base.StatementParser;
import dnars.siebog.DNarsAgent;
import static org.junit.Assert.*;

public class BeliefParserTest {
	@Test
	public void testInitialBeliefs() {
		// @formatter:off
		final String[] expected = {
			"tiger -> cat (1.0, 0.9)",
			"cat -> animal (0.9, 0.6)",
			"bird -> mammal (1.0, 0.9)",
			"bird -> animal (1.0, 0.9)"
		};
		// @formatter:on

		class TestAgent extends DNarsAgent {
			private static final long serialVersionUID = 1L;

			@Beliefs
			public Statement[] init1() {
				return new Statement[] { StatementParser.apply(expected[0]),
						StatementParser.apply(expected[1]) };
			}

			@Beliefs
			public String[] init2() {
				return new String[] { expected[2], expected[3] };
			}
		}

		TestAgent agent = new TestAgent();
		BeliefParser bp = new BeliefParser(agent);
		List<Statement> actual = bp.getInitialBeliefs();
		assertStatements(Arrays.asList(expected), actual);
	}

	private void assertStatements(List<String> expected, List<Statement> actual) {
		assertEquals(expected.size(), actual.size());
		for (String ex : expected) {
			Statement st = StatementParser.apply(ex);
			assertStatement(st, actual);
		}
	}

	private void assertStatement(Statement expected, List<Statement> actual) {
		for (Statement st : actual) {
			if (expected.equals(st)) {
				return;
			}
		}
		assertTrue("Statement " + expected + " not found.", false);
	}
}
