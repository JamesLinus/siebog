package org.xjaf2x.server.agents.ping;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.jboss.ejb3.annotation.Clustered;
import org.xjaf2x.server.agentmanager.agent.AID;
import org.xjaf2x.server.agentmanager.agent.Agent;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import org.xjaf2x.server.messagemanager.fipaacl.Performative;

@Stateful(name = "org_xjaf2x_server_agents_ping_PingAgent")
@Remote(AgentI.class)
@Clustered
public class PingAgent extends Agent 
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void onMessage(ACLMessage msg)
	{
		logger.info("Ping @ [" + System.getProperty("jboss.node.name") + "]");
		
		AID pongAid = agMngr().startAgent("org_xjaf2x_server_agents_ping_PongAgent", "Pong", null);
		ACLMessage pongMsg = new ACLMessage(Performative.REQUEST);
		pongMsg.setSender(myAid);
		pongMsg.addReceiver(pongAid);
		msgMngr().post(pongMsg);
		
		ACLMessage reply = receive(0);
		logger.info("Pong says: " + reply.getContent());
	}
}