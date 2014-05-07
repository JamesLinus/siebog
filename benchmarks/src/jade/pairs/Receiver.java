package jade.pairs;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public class Receiver extends Agent
{
	private static final long serialVersionUID = 5595224627469526099L;
	private int primeLimit;
	private int numIterations;
	
	@Override
	public void setup()
	{
		primeLimit = Integer.parseInt(getArguments()[0].toString());
		numIterations = Integer.parseInt(getArguments()[1].toString());
		
		addBehaviour(new Behaviour() {
			private static final long serialVersionUID = -4006196418013061321L;

			@Override
			public void action()
			{
				ACLMessage msg = receive();
				if (msg != null)
				{
					--numIterations;
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setSender(getAID());
					reply.setContent(msg.getContent() + "" + process());
					send(reply);
				}
				else
					block();
			}
			
			@Override
			public boolean done()
			{
				return numIterations <= 0;
			}
		});
	}
	
	private int process()
	{
		int primes = 0;
		for (int i = 1; i <= primeLimit; i++)
		{
			int j = 2;
			while (j <= i)
			{
				if (i % j == 0)
					break;
				++j;
			}
			if (j == i)
				++primes;
		}
		return primes;
	}

}
