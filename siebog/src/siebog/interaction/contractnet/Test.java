package siebog.interaction.contractnet;

import siebog.SiebogClient;
import siebog.agents.AID;
import siebog.agents.AgentBuilder;
import siebog.agents.AgentClass;
import siebog.core.Global;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.utils.ObjectFactory;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class Test {
	private static final int NUM_PARTICIPANTS = 8;

	public static void main(String[] args) {
		SiebogClient.connect("localhost");
		AID[] participants = createParticipants();
		AID initiator = createInitiator();
		start(initiator, participants);
	}

	private static AID[] createParticipants() {
		return AgentBuilder
			.siebog()
			.ejb(ParticipantImpl.class)
			.startNInstances(NUM_PARTICIPANTS).toArray(new AID[0]);
	}

	private static AID createInitiator() {
		return AgentBuilder.siebog().ejb(InitiatorImpl.class).start();
	}

	private static void start(AID initiator, AID[] participants) {
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.receivers.add(initiator);
		//msg.contentObj = participants;
		ObjectFactory.getMessageManager().post(msg);
		
	}
}
