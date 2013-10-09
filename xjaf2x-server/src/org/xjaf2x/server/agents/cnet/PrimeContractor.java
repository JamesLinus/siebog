package org.xjaf2x.server.agents.cnet;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import org.jboss.ejb3.annotation.Clustered;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.agents.protocols.cnet.CNetContractor;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import org.xjaf2x.server.messagemanager.fipaacl.Performative;

@Stateful(name = "org_xjaf2x_server_agents_cnet_PrimeContractor")
@Remote(AgentI.class)
@Clustered
public class PrimeContractor extends CNetContractor
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(PrimeContractor.class.getName());

	@PostConstruct
	public void postConstruct()
	{
		if (logger.isLoggable(Level.INFO))
			logger.info("CNetContractor started @" + System.getProperty("jboss.node.name"));
	}
	
	private String process(String content)
	{
		long sum = 0;
		for (int i = 0; i < content.length(); i++)
			sum += content.codePointAt(i);
		return "" + sum;
	}
	
	@Override
	protected ACLMessage getProposal(ACLMessage cfp)
	{
		ACLMessage proposal = cfp.makeReply(Performative.PROPOSE);
		proposal.setContent(process((String) cfp.getContent()));
		return proposal;
	}

	@Override
	protected ACLMessage onAcceptProposal(ACLMessage proposal)
	{
		ACLMessage result = proposal.makeReply(Performative.INFORM);
		result.setContent(process((String) proposal.getContent()));
		return result;
	}

	@Override
	protected void onRejectProposal(ACLMessage proposal)
	{
	}
	
	@Override
	@Remove
	public void terminate()
	{
	}
}
