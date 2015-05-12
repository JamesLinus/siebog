package siebog.interaction.blackboard.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import siebog.SiebogClient;
import siebog.agents.AID;
import siebog.agents.AgentBuilder;
import siebog.agents.AgentClass;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.interaction.blackboard.Event;
import siebog.starter.Global;
import siebog.utils.ObjectFactory;

/**
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic<a>
 */
public class Test {
	private static final int NUM_GENERATE = 2;
	private static final int NUM_SQUARE = 3;
	private static final int NUM_PRINT = 2;

	public static void main(String[] args) {
		SiebogClient.connect("localhost");
		List<AID> kss =createKSs();
		AID blackboard = createBlackboard();
		start(blackboard,kss);
	}

	private static List<AID> createKSs() {
		//GenerateKS
		Set<AID> aids = AgentBuilder
			.siebog()
			.ejb(GenerateKS.class)
			.startNInstances(NUM_GENERATE);
		//SquareKS
		aids.addAll(AgentBuilder
			.siebog()
			.ejb(SquareKS.class)
			.startNInstances(NUM_SQUARE));
		//PrintKS
		aids.addAll(AgentBuilder
			.siebog()
			.ejb(PrintKS.class)
			.startNInstances(NUM_PRINT));
		List<AID> list = new ArrayList<>();
		list.addAll(aids);
		return list;
	}

	private static AID createBlackboard() {
		//return AgentBuilder.siebog().ejb(InitiatorExample.class).start();
		AgentClass icls = new AgentClass(Global.SIEBOG_MODULE, BlackboardExample.class.getSimpleName());
		return ObjectFactory.getAgentManager().startServerAgent(icls, "Numbers", null);
	}

	private static void start(AID blackboardAid,List<AID> list) {
		ACLMessage msg = new ACLMessage(Performative.INFORM);
		msg.content="Test";
		msg.receivers.add(blackboardAid);
		Event e = new Event();
		e.setName("START");
		e.setKSs(list);
		msg.contentObj=e;
		ObjectFactory.getMessageManager().post(msg);

	}
}
