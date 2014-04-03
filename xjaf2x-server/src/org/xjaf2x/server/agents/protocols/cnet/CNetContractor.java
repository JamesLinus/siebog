package org.xjaf2x.server.agents.protocols.cnet;

import org.xjaf2x.server.agentmanager.agent.Agent;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;

/**
 * Base class for <a
 * href="http://www.fipa.org/specs/fipa00029/SC00029H.pdf">FIPA Contract Net</a>
 * contractor/participant agents.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class CNetContractor extends Agent
{
	private static final long serialVersionUID = 1L;

	@Override
	public void onMessage(ACLMessage message)
	{
		switch (message.getPerformative())
		{
		case CALL_FOR_PROPOSAL:
			ACLMessage reply = getProposal(message);
			if (reply != null)
				msgMngr().post(reply);
			break;
		case ACCEPT_PROPOSAL:
			ACLMessage result = onAcceptProposal(message);
			if (result != null)
				msgMngr().post(result);
			break;
		case REJECT_PROPOSAL:
			onRejectProposal(message);
			break;
		default:
			break;
		}

	}

	protected abstract ACLMessage getProposal(ACLMessage cfp);

	protected abstract ACLMessage onAcceptProposal(ACLMessage proposal);

	protected abstract void onRejectProposal(ACLMessage proposal);
}
