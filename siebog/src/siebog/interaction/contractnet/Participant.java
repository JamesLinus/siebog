package siebog.interaction.contractnet;

import siebog.interaction.ACLMessage;

/**
 * 
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic</a>
 */

public interface Participant {
	
	public void propose(ACLMessage msg, String val);
	
	public void refuse(ACLMessage msg);
	
/*	public void faliure();
	
	public void inform_done();
	
	public void inform_result();*/
	
	public void handleCfp(ACLMessage cfp);
	
//	public void handleRejectProposal();
	
	public void handleAcceptProposal(ACLMessage msg);
	
	
}
