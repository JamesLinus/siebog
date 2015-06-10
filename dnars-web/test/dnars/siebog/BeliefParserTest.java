package dnars.siebog;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import dnars.base.Statement;
import dnars.base.StatementParser;
import dnars.siebog.annotations.Beliefs;

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
		DNarsAgent agent = new DNarsAgent() {
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
		};
		BeliefParser bp = new BeliefParser(agent);
		List<Statement> actual = bp.getInitialBeliefs();
		TestUtils.assertStatements(Arrays.asList(expected), actual);
	}
}
