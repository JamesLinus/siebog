package dnars.siebog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import dnars.base.Statement;
import dnars.base.StatementParser;
import dnars.events.EventPayload;
import dnars.events.EventPayload.Type;
import dnars.siebog.EventParser.ParsedMethod;

public class EventHandler implements Serializable {
	private static final long serialVersionUID = 1L;
	private DNarsAgent agent;
	private EventParser parser;

	public EventHandler(DNarsAgent agent) {
		this.agent = agent;
		this.parser = new EventParser(agent);
	}

	public void handle(EventPayload[] events) {
		List<Statement> added = new ArrayList<>();
		List<Statement> updated = new ArrayList<>();
		split(events, added, updated);
		notify(added, parser.getAdded());
		notify(updated, parser.getUpdated());
	}

	private void split(EventPayload[] events, List<Statement> added, List<Statement> updated) {
		for (EventPayload ev : events) {
			if (ev.getType() == Type.ADDED) {
				added.add(StatementParser.apply(ev.getStatement()));
			} else {
				updated.add(StatementParser.apply(ev.getStatement()));
			}
		}
	}

	private void notify(List<Statement> statements, List<ParsedMethod> added) {
		try {
			for (ParsedMethod m : added) {
				List<Statement> st = getMatches(m, statements);
				if (st.size() > 0) {
					m.ref.invoke(agent, st);
				}
			}
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private List<Statement> getMatches(ParsedMethod m, List<Statement> statements) {
		List<Statement> result = new ArrayList<>();
		for (Statement st : statements) {
			if (st.subj().toString().matches(m.subj) && st.copula().matches(m.copula)
					&& st.pred().toString().matches(m.pred)
					&& st.truth().toString().matches(m.truth)) {
				result.add(st);
			}
		}
		return result;
	}
}
