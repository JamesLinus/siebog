package org.xjaf2x.server.agents.ping;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.jboss.ejb3.annotation.Clustered;
import org.xjaf2x.server.agentmanager.agent.Agent;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import org.xjaf2x.server.messagemanager.fipaacl.Performative;

@Stateful(name = "org_xjaf2x_server_agents_ping_PongAgent")
@Remote(AgentI.class)
@Clustered
public class PongAgent extends Agent
{
	private static final long serialVersionUID = 1L;
	private int number = 0;
	
	@Override
	protected void onMessage(ACLMessage msg)
	{
		logger.info("Pong @ [" + System.getProperty("jboss.node.name") + "]");
		
		ACLMessage reply = msg.makeReply(Performative.INFORM);
		reply.setContent(number++);
		msgMngr().post(reply);
	}
}
