package siebog.agents.test.sequencers;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;
import siebog.agents.AID;
import siebog.agents.AgentClass;
import siebog.agents.AgentInitArgs;
import siebog.agents.AgentManager;
import siebog.agents.test.TestClientBase;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.utils.ObjectFactory;

public class SequencerMain extends TestClientBase {
	private AgentClass agClass;
	private AgentManager agm;

	public SequencerMain() throws RemoteException {
		agClass = AgentClass.forSiebogEjb(Sequencer.class);
		agm = ObjectFactory.getAgentManager();
	}

	public void go() throws InterruptedException {
		final int numAgents = 16;
		final int number = (int) (Math.random() * 1000);
		createAgents(numAgents);
		sendMessages(number);
		ACLMessage reply = msgQueue.poll(10, TimeUnit.MINUTES);
		int got = Integer.parseInt(reply.content);
		System.out.printf("%d + %d = %d?\n", number, numAgents, got);
	}

	private void createAgents(int n) {
		for (int i = 0; i < n - 1; i++) {
			AID next = new AID(getName(i + 1), AgentClass.forSiebogEjb(Sequencer.class));
			createAgent(i, next);
		}
		createAgent(n - 1, testAgentAid);
	}

	private String getName(int index) {
		return "Seq-" + index;
	}

	private void createAgent(int index, AID next) {
		AgentInitArgs args = new AgentInitArgs();
		args.put("next", next.toString());
		agm.startServerAgent(agClass, getName(index), args);
	}

	private int sendMessages(int number) {
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.content = String.valueOf(number);
		msg.receivers.add(new AID(getName(0), agClass));
		ObjectFactory.getMessageManager().post(msg);
		return number;
	}

	public static void main(String[] args) throws RemoteException, InterruptedException {
		new SequencerMain().go();
	}
}
