package siebog.interaction.contractnet;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import siebog.agents.AID;
import siebog.agents.Agent;
import siebog.agents.XjafAgent;
import siebog.core.Global;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.utils.ObjectFactory;

/**
 * 
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic</a>
 */

@Stateful
@Remote(Initiator.class)
public class InitiatorImpl extends XjafAgent implements Initiator {

	private static final long serialVersionUID = 1L;
	private int pendingProposals;
	private ACLMessage bestProposal;
	private List<ACLMessage> rejectedMsg = new ArrayList<ACLMessage>();
	private List<AID> rejectedAID = new ArrayList<AID>();
	private int bestValue = 10000;
	private FaultCheckingService fault;
	private boolean waiting = false;
	private long replyBy;
	private boolean done;

	private FaultCheckingService getFaultTimer() {
		final String name = "ejb:/" + Global.SIEBOG_MODULE + "//"
				+ FaultCheckingBean.class.getSimpleName() + "!"
				+ FaultCheckingService.class.getName();
		FaultCheckingService timer = ObjectFactory.lookup(name,
				FaultCheckingService.class);
		return timer;

	}

	@Override
	public void cfp() {

		List<AID> participants = ObjectFactory.getAgentManager()
				.getRunningAgents();
		// AID[] participants = (AID[]) msg.contentObj;
		ACLMessage msg = new ACLMessage(Performative.CALL_FOR_PROPOSAL);
		msg.receivers.addAll(participants);
		msg.content = "10;9;8";
		msg.sender = myAid;
		long duration = 10000; // it will wait 10 seconds
		replyBy = System.currentTimeMillis() + duration;

		msg.replyBy = replyBy;
		msm().post(msg);
		pendingProposals = participants.size() - 1;
		logger.info("A call for proposals is out!");
		fault = getFaultTimer();
		fault.createReplyByTimer(myAid, duration);
	}

	@Override
	public void rejectProposal() {
		ACLMessage msg = new ACLMessage(Performative.REJECT_PROPOSAL);
		msg.sender = myAid;
		msg.receivers = rejectedAID;
		msm().post(msg);
		rejectedAID.clear();
		rejectedMsg.clear();
		bestValue = 10000;
		bestProposal = null;
		fault.cancelTimers();

	}

	@Override
	public void acceptProposal() {
		ACLMessage msg = new ACLMessage(Performative.ACCEPT_PROPOSAL);
		msg.sender = myAid;
		if (bestProposal != null) {
			msg.receivers.add(bestProposal.sender);
			waiting = true;
			fault.createWaitingTimer(myAid, bestProposal.sender, bestValue);
			msm().post(msg);
			logger.info("Accepting proposal with value " + bestValue);
		} else {
			logger.info("No proposals made");
		}
	}

	@Override
	public void handleRefuse() {
		pendingProposals--;
		if (pendingProposals == 0) {
			acceptProposal();
		}
	}

	@Override
	public void handlePropose(ACLMessage msg) {
		if (bestValue > Integer.parseInt(msg.content)) {
			if (bestProposal != null) {
				rejectedMsg.add(bestProposal);
				rejectedAID.add(bestProposal.sender);
			}
			bestValue = Integer.parseInt(msg.content);

			bestProposal = msg;
		} else {
			rejectedMsg.add(msg);
			rejectedAID.add(msg.sender);
		}
		pendingProposals--;
		if (pendingProposals == 0) {
			acceptProposal();
		}
	}

	@Override
	public void handleFailure() {
		if (waiting)
			fault.cancelTimerWait(bestValue, bestProposal.sender);
		int min = 10000;
		int pos = -1;
		for (int i = 0; i < rejectedMsg.size(); i++) {
			ACLMessage msg = rejectedMsg.get(i);
			if (min > Integer.parseInt(msg.content)) {
				pos = i;
				min = Integer.parseInt(msg.content);
			}
		}
		if (pos == -1) {
			logger.info("No contractor was able to preform the action.");
			done = true;
		} else {
			bestValue = min;
			bestProposal = rejectedMsg.get(pos);
			rejectedMsg.remove(pos);
			rejectedAID.remove(pos);
			acceptProposal();
		}
	}

	@Override
	public void handleDone(ACLMessage msg) {
		int sum = Integer.parseInt(msg.content);
		logger.info("Sum is " + sum);
		waiting = false;
		done = true;
		rejectProposal();

	}

	@Override
	protected void onMessage(ACLMessage msg) {
		switch (msg.performative) {

		case REQUEST:
			cfp();
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
		default:
			break;
		}
	}

	@Override
	public String ping() {
		if (System.currentTimeMillis() > replyBy && !waiting && !done) {
			logger.info("ReplyBy time has passed.");
			pendingProposals = 0;
			acceptProposal();
		} else if (waiting && !done) {
			logger.info("Participant hasn't finished in time.");
			waiting = false;
			handleFailure();
		}
		return super.ping();
	}

}
