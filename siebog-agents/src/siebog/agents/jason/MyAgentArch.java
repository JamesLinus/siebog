package siebog.agents.jason;

import jason.architecture.AgArch;
import jason.asSemantics.ActionExec;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class MyAgentArch extends AgArch implements Serializable {
	private static final long serialVersionUID = 1L;
	private boolean first = true;

	public MyAgentArch() {
	}

	@Override
	public List<Literal> perceive() {
		if (first) {
			first = false;
			Literal l = Literal.parseLiteral("hello(42)");
			return Collections.singletonList(l);
		}
		return Collections.emptyList();
	}

	@Override
	public void act(ActionExec action, List<ActionExec> feedback) {
		final Structure term = action.getActionTerm();
		final String func = term.getFunctor();
		if (func.equals("doPrint")) {
			System.out.println(term.getTerm(0));
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			action.setResult(true);
		} else {
			action.setResult(false);
			action.setFailureReason(Literal.parseLiteral("notRecognized"), "Action not recognized.");
		}
		feedback.add(action);
	}
}
