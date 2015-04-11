package siebog.interaction.contractnet;

import java.util.ArrayList;
import java.util.List;
import siebog.agents.AID;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.utils.ObjectFactory;

/**
 * 
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic</a>
 */

public abstract class Initiator extends XjafAgent {

	private static final long serialVersionUID = 1L;
	private int pendingProposals;
	private Proposal bestProposal;
	private List<AID> rejectedAID = new ArrayList<AID>();
	
	private List<Proposal> proposals = new ArrayList<Proposal>();

	/*
	 * 1 - all proposals received
	 * 2 - acceptance sent, waiting for response
	 * 3 - all done
	 */
	private int status = 0;

	public void cfp(CallForProposal proposal) {
		status = 0;
		proposal.setInitiator(myAid);
		List<AID> participants = ObjectFactory.getAgentManager()
				.getRunningAgents();
		ACLMessage msg = new ACLMessage(Performative.CALL_FOR_PROPOSAL);
		msg.receivers.addAll(participants);
		msg.contentObj = proposal;
		msg.sender = myAid;
		
		msg.replyBy = proposal.getReplyBy();
		msm().post(msg);
		pendingProposals = participants.size() - 1;
		logger.info("A call for proposals is out!");
		//fault checking
		ACLMessage delayedMsg = new ACLMessage();
		delayedMsg.sender=myAid;
		delayedMsg.receivers.add(myAid);
		delayedMsg.content = "replyBy";
		delayedMsg.contentObj = new Delay(proposal.getReplyBy());
		msm().post(delayedMsg);
	}

	public void rejectProposal() {
		
		ACLMessage msg = new ACLMessage(Performative.REJECT_PROPOSAL);
		msg.sender = myAid;
		msg.receivers = rejectedAID;
		msm().post(msg);
		rejectedAID.clear();
		bestProposal = null;
	}

	public void acceptProposal() {
		ACLMessage msg = new ACLMessage(Performative.ACCEPT_PROPOSAL);
		msg.sender = myAid;
		
		bestProposal = getOptimalProposal(proposals);
		proposals.remove(bestProposal);
		rejectedAID.remove(bestProposal.getParticipant());
		
		if (bestProposal != null) {
			msg.receivers.add(bestProposal.getParticipant());
			status = 2;
			msm().post(msg);
			

			//fault checking
			ACLMessage delayedMsg = new ACLMessage();
			delayedMsg.sender=myAid;
			delayedMsg.receivers.add(myAid);
			Delay delay = new Delay(bestProposal.getTimeEstimation());
			delay.setWaitingOn(bestProposal.getParticipant());
			delayedMsg.contentObj = delay;
			delayedMsg.content = "waiting";
			msm().post(delayedMsg);

		} else {
			logger.info("No proposals made");
		}
	}

	public void handleRefuse() {
		pendingProposals--;
		if (pendingProposals == 0) {
			acceptProposal();
		}
	}

	public void handlePropose(ACLMessage msg) {
		
		proposals.add((Proposal)msg.contentObj);
		rejectedAID.add(((Proposal)msg.contentObj).getParticipant());
		pendingProposals--;
		if (pendingProposals == 0) {
			acceptProposal();
		}
	}
	
	public abstract Proposal getOptimalProposal(List<Proposal> proposals);	
	public abstract void failure();
	public abstract void success(Result result);
	
	public void handleFailure() {
		bestProposal = getOptimalProposal(proposals);
		
		if (bestProposal==null) {
			status = 3;
			failure();
		} else {
			proposals.remove(bestProposal);
			rejectedAID.remove(bestProposal.getParticipant());
			acceptProposal();
		}
	}

	public void handleDone(ACLMessage msg) {
		status = 3;
		rejectProposal();
		success((Result) msg.contentObj);
	}
	
	public abstract CallForProposal createCfp();

	@Override
	protected void onMessage(ACLMessage msg) {
		switch (msg.performative) {

		case REQUEST:
			CallForProposal cfp = createCfp();
			cfp(cfp);
			break;
		case PROPOSE:
			handlePropose(msg);
			break;
		case REFUSE:
			handleRefuse();
			break;
		case INFORM:
			handleDone(msg);
			break;
		case FAILURE:
			handleFailure();
			break;
		case CALL_FOR_PROPOSAL:
			break;
		default: //delayedMsg
			if (msg.content.equals("replyBy")&&status<1){
				logger.info("ReplyBy time has passed.");
				pendingProposals = 0;
				acceptProposal();

			} else if (msg.content.equals("waiting")&& status>1){
				if (bestProposal!=null && ((Delay)msg.contentObj).getWaitingOn().equals(bestProposal.getParticipant())){
					logger.info("Participant hasn't finished in time." +  ((Delay)msg.contentObj).getWaitingOn());
					handleFailure();
				}
			}
			break;
		}
	}

}
