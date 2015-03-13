package siebog.agents.test.wc;

import java.util.HashSet;
import java.util.Set;
import siebog.SiebogClient;
import siebog.agents.AID;
import siebog.agents.AgentClass;
import siebog.agents.AgentManager;
import siebog.core.Global;
import siebog.interaction.ACLMessage;
import siebog.interaction.MessageManager;
import siebog.interaction.Performative;
import siebog.utils.ObjectFactory;

public class WCStarter {
	public static void main(String[] args) throws InterruptedException {
		SiebogClient.connect("localhost"); // "192.168.213.1", "192.168.213.129");
		AgentManager agm = ObjectFactory.getAgentManager();
		MessageManager msm = ObjectFactory.getMessageManager();

		AgentClass cls = new AgentClass(Global.SIEBOG_MODULE, WordCounter.class.getSimpleName());

		final int numAgents = 4;
		Set<AID> aids = new HashSet<>();
		for (int i = 0; i < numAgents; i++) {
			AID aid = agm.startAgent(cls, "WC-" + i, null);
			aids.add(aid);
		}

		// while (true) {
		for (AID aid : aids) {
			ACLMessage msg = new ACLMessage(Performative.REQUEST);
			msg.receivers.add(aid);
			msg.content = "/home/dejan/paper.tex";
			msm.post(msg);
		}
		// Thread.sleep(1000);
		// }

		// String name = "ejb:/" + Global.SIEBOG_MODULE + "//" + WordCounter.class.getSimpleName()
		// + "!" + Agent.class.getName() + "?stateful";
		//
		// List<Agent> list = new ArrayList<>();
		// for (int i = 0; i < 4; i++) {
		// Agent a = ObjectFactory.lookup(name, Agent.class);
		// a.init(new AID("A-" + i, cls), null);
		// list.add(a);
		// }
		//
		// while (true) {
		// for (Agent a : list)
		// try {
		// a.ping();
		// } catch (Exception ex) {
		// System.out.println(ex.getMessage());
		// }
		// Thread.sleep(1000);
		// }
	}
}
