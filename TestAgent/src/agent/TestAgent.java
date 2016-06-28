package agent;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import siebog.agentmanager.Agent;
import siebog.agentmanager.XjafAgent;
import siebog.messagemanager.ACLMessage;
import siebog.utils.LoggerUtil;

@Stateless
@Remote(Agent.class)
public class TestAgent extends XjafAgent {
	private static final long serialVersionUID = 3578264688316829461L;

	@Override
	protected void onMessage(ACLMessage msg) {
		//System.out.println("2Message to TestAgent: " + msg);
		//System.out.println("2Message content: " + msg.content);
		LoggerUtil.log("2Message to TestAgent: " + msg, true);
		LoggerUtil.log("2Message content: " + msg.content, true);
	}
}
