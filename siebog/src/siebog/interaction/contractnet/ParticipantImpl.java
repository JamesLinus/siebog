package siebog.interaction.contractnet;


import java.util.Random;

/**
 * 
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic</a>
 */

import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.agents.Agent;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;

@Stateful
@Remote(Agent.class)
public class ParticipantImpl  extends XjafAgent  implements Participant {

	String[] nums;
	Random rnd = new Random();

	private static final long serialVersionUID = 1L;

	@Override
	public void propose(ACLMessage msg,String num) {
		logger.info(myAid + ": My bid is " + num);
		ACLMessage reply = new ACLMessage(Performative.PROPOSE);
		reply.receivers.add(msg.sender);
		reply.content = num;
		reply.sender = myAid;
		msm().post(reply);

	}

	@Override
	public void refuse(ACLMessage msg) {
		logger.info(myAid + ": I'm not bidding.");
		ACLMessage reply = new ACLMessage(Performative.REFUSE);

		reply.receivers.add(msg.sender);

		reply.sender = myAid;
		msm().post(reply);

	}


	@Override
	public void handleCfp(ACLMessage msg) {
		if (msg.replyBy<System.currentTimeMillis())
			logger.info(myAid + ": ReplyBy time has elapsed.");
		else {

			int num = rnd.nextInt(20-1+1)+1;
			nums = msg.content.split(";");
			if (num<Integer.parseInt(nums[0])){
				refuse(msg);
			} else {
				propose(msg,Integer.toString(num));
			}
		}
	}

	/*	@Override
	public void handleRejectProposal() {
		//maybe unsubscribe from topic???? if creating new topic possible, 
		//although it doesn't appear to be
	}*/

	@Override
	public void handleAcceptProposal(ACLMessage msga) {
		logger.info(myAid + ": My bid was accepted.");
		int sum = 0;
		for (int i = 1;i<nums.length;i++){
			sum+=Integer.parseInt(nums[i]);
		}
		ACLMessage msg=null;
		int num = rnd.nextInt(20-1+1)+1;
		if (sum!=0 && num>10){
			msg = new ACLMessage(Performative.INFORM);
			msg.sender = myAid;
			msg.receivers.add(msga.sender);
			msg.content=Integer.toString(sum);
			logger.info(myAid + ": Task succesfully preformed.");
			msm().post(msg);
		} else {
			msg = new ACLMessage(Performative.FAILURE);
			msg.sender = myAid;
			msg.receivers.add(msga.sender);
			logger.info(myAid + ": Failure in preforming the task.");
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
			handleAcceptProposal(msg);
			break;
		default: 
			break;
		}
	}
}
