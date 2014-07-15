package radigost.samples.jade.rtt.pairs;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class Receiver extends Agent
{
	private static final long serialVersionUID = 1L;
	private int limit;

	@Override
	public void setup()
	{
		limit = (Integer) getArguments()[0];
		
		addBehaviour(new CyclicBehaviour() {
			private static final long serialVersionUID = 1L;

			@Override
			public void action()
			{
				ACLMessage msg = myAgent.receive();
				if (msg == null)
					block();
				else
				{
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setSender(getAID());
					reply.setContent(msg.getContent() + process());
					send(reply);
				}
			}
		});
	}
	
	private int process()
	{
		int primes = 0;
		
		for (int i = 1; i <= limit; i++) {
			int j = 2;
			while (j <= i) {
				if (i % j == 0)
					break;
				j++;
			}
			if (j == i)
				primes++;
		}
		
		return primes;
	}
}
