package radigost.samples.jade.cnet;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class Contractor extends Agent
{
	private static final long serialVersionUID = 1L;
	
	private int process(String content)
	{
		int sum = 0;
		for (int i = 0; i < content.length(); i++)
			sum += content.codePointAt(i);
		return sum;
	}
	
	@Override
	protected void setup()
	{
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
					reply.setContent("" + process(msg.getContent()));
					int p = msg.getPerformative() == ACLMessage.CFP ? ACLMessage.PROPOSE : ACLMessage.INFORM;
					reply.setPerformative(p);
					send(reply);
				}
			}
		});
		
		System.out.println("Ready " + getAID());
	}
}
