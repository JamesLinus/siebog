package siebog.agents.test.wordcounter;

import siebog.agents.AID;
import siebog.agents.Agent;
import siebog.agents.AgentClass;
import siebog.agents.test.TestClientBase;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;

public class WordCounterTest extends TestClientBase {
	private final int NUMBER_OF_AGENTS = 4;
	
	@Override
	public void test() {
		for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
			AID aid = agm.startServerAgent(new AgentClass(Agent.SIEBOG_MODULE, WordCounter.class.getSimpleName()), "WordCounter" + i, null);
			
			ACLMessage msg = new ACLMessage(Performative.REQUEST);
			msg.sender = testAgentAid;
			msg.receivers.add(aid);
			msg.content = "text" + i + ".txt";
			
			msm.post(msg);
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		new WordCounterTest().test();
	}
}
