package siebog.interaction.contractnet.example;

import siebog.SiebogClient;
import siebog.agents.AID;
import siebog.agents.AgentBuilder;
import siebog.agents.AgentClass;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.starter.Global;
import siebog.utils.ObjectFactory;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class Test {
	private static final int NUM_PARTICIPANTS = 4;

	public static void main(String[] args) {
		SiebogClient.connect("localhost");
		AID[] participants = createParticipants();
		AID initiator = createInitiator();
		start(initiator, participants);
	}

	private static AID[] createParticipants() {
		return AgentBuilder
			.siebog()
			.ejb(ParticipantExample.class)
			.startNInstances(NUM_PARTICIPANTS).toArray(new AID[0]);
	}

	private static AID createInitiator() {
		//return AgentBuilder.siebog().ejb(InitiatorExample.class).start();
		AgentClass icls = new AgentClass(Global.SIEBOG_MODULE, InitiatorExample.class.getSimpleName());
		return ObjectFactory.getAgentManager().startServerAgent(icls, "I1", null);
	}

	private static void start(AID initiator, AID[] participants) {
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.receivers.add(initiator);
		ObjectFactory.getMessageManager().post(msg);
		
	}
}
