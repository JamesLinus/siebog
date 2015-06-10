package dnars.siebog;

import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Test;
import dnars.base.Statement;
import dnars.siebog.EventParser.ParsedMethod;
import dnars.siebog.annotations.BeliefAdded;
import dnars.siebog.annotations.BeliefUpdated;

public class EventParserTest {
	@Test
	public void testBeliefsMethods() {
		DNarsAgent ag = new DNarsAgent() {
			private static final long serialVersionUID = 1L;

			@BeliefAdded(subj = "cat", copula = "->", pred = "animal")
			public void onBeliefAdded(List<Statement> beliefs) {
			}

			@BeliefUpdated
			public void onBeliefUpdated(List<Statement> beliefs) {
			}

			@SuppressWarnings("unused")
			public void dummy() {
			}
		};
		EventParser parser = new EventParser(ag);
		assertAdded(parser.getAdded());
		assertUpdated(parser.getUpdated());
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidHeader() {
		DNarsAgent ag = new DNarsAgent() {
			private static final long serialVersionUID = 1L;

			@BeliefAdded
			public void invalid(String[] statements) {
			}
		};
		new EventParser(ag);
	}

	private void assertAdded(List<ParsedMethod> list) {
		assertEquals(1, list.size());
		ParsedMethod m = list.get(0);
		assertEquals("onBeliefAdded", m.ref.getName());
		assertEquals("cat", m.subj);
		assertEquals("->", m.copula);
		assertEquals("animal", m.pred);
		assertEquals(".*", m.truth);
	}

	private void assertUpdated(List<ParsedMethod> list) {
		assertEquals(1, list.size());
		ParsedMethod m = list.get(0);
		assertEquals("onBeliefUpdated", m.ref.getName());
		assertEquals(".*", m.subj);
		assertEquals(".*", m.copula);
		assertEquals(".*", m.pred);
		assertEquals(".*", m.truth);
	}
}
