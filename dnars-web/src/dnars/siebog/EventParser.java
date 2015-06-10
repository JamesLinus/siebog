package dnars.siebog;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import dnars.siebog.annotations.BeliefAdded;
import dnars.siebog.annotations.BeliefUpdated;

public class EventParser implements Serializable {
	private static final long serialVersionUID = 1L;

	public static class ParsedMethod implements Serializable {
		private static final long serialVersionUID = 1L;
		Method ref;
		String subj;
		String copula;
		String pred;
		String truth;
	}

	private List<ParsedMethod> added;
	private List<ParsedMethod> updated;

	public EventParser(DNarsAgent agent) {
		added = new ArrayList<>();
		updated = new ArrayList<>();
		parse(agent);
	}

	public List<ParsedMethod> getAdded() {
		return added;
	}

	public List<ParsedMethod> getUpdated() {
		return updated;
	}

	private void parse(DNarsAgent agent) {
		for (Method m : agent.getClass().getMethods()) {
			if (m.isAnnotationPresent(BeliefAdded.class)) {
				checkArgs(m);
				parseBeliefAdded(m);
			} else if (m.isAnnotationPresent(BeliefUpdated.class)) {
				checkArgs(m);
				parseBeliefUpdated(m);
			}
		}
	}

	private void checkArgs(Method m) {
		Class<?>[] types = m.getParameterTypes();
		if (m.getParameterCount() != 1 || !types[0].equals(List.class)) {
			throw new IllegalStateException("Method " + m.getName()
					+ " should only receive an array of Statement objects.");
		}
	}

	private void parseBeliefAdded(Method m) {
		ParsedMethod pm = new ParsedMethod();
		pm.ref = m;
		BeliefAdded ann = m.getAnnotation(BeliefAdded.class);
		pm.subj = ann.subj();
		pm.copula = ann.copula();
		pm.pred = ann.pred();
		pm.truth = ann.truth();
		added.add(pm);
	}

	private void parseBeliefUpdated(Method m) {
		ParsedMethod pm = new ParsedMethod();
		pm.ref = m;
		BeliefUpdated ann = m.getAnnotation(BeliefUpdated.class);
		pm.subj = ann.subj();
		pm.copula = ann.copula();
		pm.pred = ann.pred();
		pm.truth = ann.truth();
		updated.add(pm);
	}
}
