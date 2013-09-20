package org.xjaf2x.server.agents.ping;

import java.util.logging.Logger;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import org.jboss.ejb3.annotation.Clustered;
import org.xjaf2x.server.agentmanager.agent.Agent;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;

@Stateless
@Remote(AgentI.class)
@Clustered
public class PingAgent extends Agent 
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(PingAgent.class.getName());

	@Override
	@Lock(LockType.WRITE)
	public void onMessage(ACLMessage message)
	{
		String nodeName = System.getProperty("jboss.node.name");
		String content = "Pong from [" + nodeName + "]";
		logger.info(content);
	}
}