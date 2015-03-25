package siebog.agents.test.load;

import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.agents.AID;
import siebog.agents.AgentManager;
import siebog.agents.test.TestClientBase;
import siebog.interaction.ACLMessage;
import siebog.interaction.MessageManager;
import siebog.interaction.Performative;
import siebog.utils.ObjectFactory;

public class LoadTest extends TestClientBase {
	private static final Logger LOG = LoggerFactory.getLogger(LoadTest.class);

	public LoadTest() throws RemoteException {
	}

	public void go() throws InterruptedException {
		AID aid = createAgent();
		final int msgCount = 100;
		sendMessages(msgCount, aid);
		LOG.info("Sent {} messages.", msgCount);
		waitFor(msgCount);
	}

	private AID createAgent() {
		AgentManager agm = ObjectFactory.getAgentManager();
		return agm.startAgent(LoadAgent.class, "LoadAgent", null);
	}

	private void sendMessages(int count, AID aid) {
		MessageManager msm = ObjectFactory.getMessageManager();
		for (int i = 0; i < count; i++) {
			ACLMessage msg = getMessage(aid, i);
			msm.post(msg);
		}
	}

	private ACLMessage getMessage(AID aid, int index) {
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.sender = testAgentAid;
		msg.receivers.add(aid);
		msg.content = String.valueOf(index);
		return msg;
	}

	private void waitFor(int msgCount) throws InterruptedException {
		for (int i = 0; i < msgCount; i++) {
			ACLMessage msg = msgQueue.poll(10, TimeUnit.MINUTES);
			LOG.info("Got: {}", msg.content);
		}
	}

	public static void main(String[] args) throws RemoteException, InterruptedException {
		new LoadTest().go();
	}
}
