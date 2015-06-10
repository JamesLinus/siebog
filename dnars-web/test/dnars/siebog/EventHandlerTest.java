package dnars.siebog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import dnars.base.Statement;
import dnars.base.StatementParser;
import dnars.events.EventPayload;
import dnars.events.EventPayload.Type;
import dnars.siebog.annotations.BeliefAdded;
import dnars.siebog.annotations.BeliefUpdated;
import dnars.siebog.annotations.Beliefs;

class EHAgentTest extends DNarsAgent {
	private static final long serialVersionUID = 1L;
	private List<Statement> added = new ArrayList<>();
	private List<Statement> updated = new ArrayList<>();

	@Beliefs
	public String[] init() {
		return new String[] { "cat -> animal (1.0, 0.9)", "cat -> mammal (1.0, 0.9)",
				"cat ~ feline (1.0, 0.9)" };
	}

	@BeliefAdded(subj = "cat", copula = "->", pred = "animal")
	public void catIsTypeOfAnimal(List<Statement> beliefs) {
		added.addAll(beliefs);
	}

	@BeliefUpdated
	public void beliefUpdated(List<Statement> beliefs) {
		updated.addAll(beliefs);
	}

	public List<Statement> getAdded() {
		return added;
	}

	public List<Statement> getUpdated() {
		return updated;
	}
}

public class EventHandlerTest {
	@Test
	public void testEventHandler() {
		EHAgentTest ag = new EHAgentTest();
		List<Statement> initial = new BeliefParser(ag).getInitialBeliefs();

		EventHandler handler = new EventHandler(ag);
		handler.handle(st2ev(initial, Type.ADDED));

		Statement updated = StatementParser.apply("bird -> animal (1.0, 0.9)");
		handler.handle(st2ev(Arrays.asList(updated), Type.UPDATED));

		String[] expectedAdded = { "cat -> animal (1.0, 0.9)" };
		TestUtils.assertStatements(Arrays.asList(expectedAdded), ag.getAdded());
		TestUtils.assertStatement(updated, ag.getUpdated());
	}

	private EventPayload[] st2ev(List<Statement> statements, Type type) {
		EventPayload[] events = new EventPayload[statements.size()];
		for (int i = 0; i < events.length; i++) {
			String st = statements.get(i).toString();
			events[i] = new EventPayload(type, st);
		}
		return events;
	}
}
