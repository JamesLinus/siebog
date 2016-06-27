package siebog.agents.test.contractnet;

import siebog.agents.AID;
import siebog.agents.Agent;
import siebog.agents.AgentClass;
import siebog.agents.test.TestClientBase;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.utils.ObjectFactory;

/**
 * This test class serves to demonstrate the contract net process using a multi agent environment.
 * An initiator and several participants are created. The initiator receives a messages which starts 
 * the call for proposals. This message can optionally contain the maximum duration in seconds of the 
 * first iteration of the process. This duration should be set using the content field of the message.
 * 
 * @author <a href="nikola.luburic@uns.ac.rs">Nikola Luburic</a>
 */
public class ContractNetTest extends TestClientBase {
	private static final int NUM_PARTICIPANTS = 8;

	@Override
	public void test() {
		AID initiator = createInitiator();
		AID[] participants = createParticipants();
		start(initiator, participants);
	}
	
	private AID[] createParticipants() {
		AID[] participants = new AID[NUM_PARTICIPANTS];
		AgentClass agClass = new AgentClass(Agent.SIEBOG_MODULE, ContractNetParticipant.class.getSimpleName());
		for(int i = 0; i < NUM_PARTICIPANTS; i++) {
			participants[i] = ObjectFactory.getAgentManager().startServerAgent(agClass, "Participant" + i, null);
		}
		return participants;
	}

	private AID createInitiator() {
		AgentClass agClass = new AgentClass(Agent.SIEBOG_MODULE, ContractNetInitiator.class.getSimpleName());
		return ObjectFactory.getAgentManager().startServerAgent(agClass, "Initiator", null);
	}

	private void start(AID initiator, AID[] participants) {
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.receivers.add(initiator);
		msg.sender = testAgentAid;
		msg.content = "15";
		ObjectFactory.getMessageManager().post(msg);
	}
	
	public static void main(String[] args) {
		new ContractNetTest().test();
	}
}
