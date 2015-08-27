package siebog.agents.test.sequencers;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.agents.AID;
import siebog.agents.Agent;
import siebog.agents.AgentInitArgs;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;

@Stateful
@Remote(Agent.class)
public class Sequencer extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private AID next;

	@Override
	protected void onInit(AgentInitArgs args) {
		next = new AID(args.get("next", null));
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		sleep();
		int num = Integer.parseInt(msg.content);

		ACLMessage nextMsg = new ACLMessage(Performative.REQUEST);
		nextMsg.receivers.add(next);
		nextMsg.content = String.valueOf(num + 1);
		msm().post(nextMsg);
	}

	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
		}
	}
}
