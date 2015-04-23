package siebog.interaction.contractnet;


/**
 * 
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic</a>
 */


import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;

public abstract class Participant  extends XjafAgent {

	private CallForProposal cfp;

	private static final long serialVersionUID = 1L;

	public void propose(Proposal proposal) {
		ACLMessage reply = new ACLMessage(Performative.PROPOSE);
		reply.receivers.add(proposal.getInitiator());
		reply.sender = myAid;
		reply.contentObj = proposal;
		msm().post(reply);

	}

	public void refuse(Proposal proposal) {
		ACLMessage reply = new ACLMessage(Performative.REFUSE);

		reply.receivers.add(proposal.getInitiator());

		reply.sender = myAid;
		msm().post(reply);

	}


	public void handleCfp(ACLMessage msg) {
		if (msg.replyBy<System.currentTimeMillis())

			logger.info(myAid + ": ReplyBy time has elapsed, so I'm not bidding.");
		else {

			Proposal proposal = createProposal((CallForProposal)msg.contentObj);
			proposal.setInitiator(msg.sender);
			if (!proposal.isProposing()){
				refuse(proposal);
			} else {
				cfp = (CallForProposal)msg.contentObj;
				propose(proposal);
			}
		}
	}

	public abstract Proposal createProposal(CallForProposal cfp);
	public abstract Result performTask(CallForProposal cfp);

	public void handleAcceptProposal() {
		Result result = performTask(cfp);


		ACLMessage msg=null;
		if (result.isSuccesful()){
			msg = new ACLMessage(Performative.INFORM);
			msg.sender = myAid;
			msg.receivers.add(cfp.getInitiator());
			msg.contentObj = result;
			msm().post(msg);
		} else {
			msg = new ACLMessage(Performative.FAILURE);
			msg.sender = myAid;
			msg.receivers.add(cfp.getInitiator());
			msm().post(msg);
		}

	}

	@Override
	protected void onMessage(ACLMessage msg) {
		switch (msg.performative) {
		case CALL_FOR_PROPOSAL:
			handleCfp(msg);
			break;
		case REJECT_PROPOSAL:
			//handleRejectProposal();
			break;
		case ACCEPT_PROPOSAL:
			handleAcceptProposal();
			break;
		default: 
			break;
		}
	}
}
