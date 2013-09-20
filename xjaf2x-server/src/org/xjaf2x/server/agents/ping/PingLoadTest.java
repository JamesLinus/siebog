package org.xjaf2x.server.agents.ping;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import org.jboss.ejb3.annotation.Clustered;
import org.xjaf2x.server.JndiManager;
import org.xjaf2x.server.agentmanager.agent.AID;
import org.xjaf2x.server.agentmanager.agent.Agent;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import org.xjaf2x.server.messagemanager.fipaacl.Performative;

@Stateless
@Remote(AgentI.class)
@Clustered
public class PingLoadTest extends Agent 
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(PingAgent.class.getName());

	@Override
	public void onMessage(ACLMessage message)
	{
		for (int i = 0; i < 10; i++)
		{
			try
			{
				String name = "A_" + System.currentTimeMillis();
				AID aid = JndiManager.getAgentManager().startAgent(PingAgent.class.getName(), name);
				
				ACLMessage msg = new ACLMessage(Performative.INFORM);
				msg.addReceiver(aid);
				JndiManager.getMessageManager().post(msg);
				
				Thread.sleep(10);
			} catch (Exception ex)
			{
				logger.log(Level.WARNING, "Error", ex);
			}
		}
	}
}
