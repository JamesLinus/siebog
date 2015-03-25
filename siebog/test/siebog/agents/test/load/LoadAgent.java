package siebog.agents.test.load;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.agents.Agent;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;

@Stateful
@Remote(Agent.class)
public class LoadAgent extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(LoadAgent.class);
	private int total;

	@Override
	protected void onMessage(ACLMessage msg) {
		LOG.info("{} processing a message on node {}.", myAid.getName(), getNodeName());
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ex) {
		}
		++total;
		LOG.info("{} processed {} messages.", myAid.getName(), total);
		reply(msg);
	}

	private void reply(ACLMessage msg) {
		ACLMessage reply = msg.makeReply(Performative.INFORM);
		reply.content = msg.content;
		msm().post(reply);
	}
}
