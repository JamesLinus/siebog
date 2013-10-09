package org.xjaf2x.server.agents.cnet;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import org.jboss.ejb3.annotation.Clustered;
import org.xjaf2x.server.agentmanager.agent.Agent;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import org.xjaf2x.server.messagemanager.fipaacl.Performative;

@Stateful(name = "org_xjaf2x_server_agents_cnet_CNetContractor")
@Remote(AgentI.class)
@Clustered
public class CNetContractor extends Agent
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CNetContractor.class.getName());

	@PostConstruct
	public void postConstruct()
	{
		if (logger.isLoggable(Level.INFO))
			logger.info("CNetContractor started @" + System.getProperty("jboss.node.name"));
	}
	
	@Override
	public void onMessage(ACLMessage message)
	{		
		byte[] content = (byte[]) message.getContent();
		int sum = getSum(content);
		
		Performative p = message.getPerformative() == Performative.CALL_FOR_PROPOSAL ? Performative.PROPOSE
				: Performative.INFORM;

		ACLMessage reply = message.makeReply(p);
		reply.setSender(aid);
		reply.setContent(sum);
		messageManager.post(reply);
	}
	
	@Override
	@Remove
	public void terminate()
	{
	}

	private int getSum(byte[] content)
	{
		int sum = 0;
		for (byte b : content)
			sum += b;
		return sum;
	}
}
