package siebog.interaction.contractnet;

import siebog.agents.Agent;
import siebog.interaction.ACLMessage;

/**
 * 
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic</a>
 */

public interface Initiator extends Agent{
	
	public void cfp();
	
	public void rejectProposal();
	
	public void acceptProposal();
	
	public void handleRefuse();
	
	public void handlePropose(ACLMessage msg);
	
	public void handleFailure();
	
	public void handleDone(ACLMessage msg);
	
//	public void timeout(String reason);
	
}


