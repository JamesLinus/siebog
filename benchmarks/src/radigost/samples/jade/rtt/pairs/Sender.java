package radigost.samples.jade.rtt.pairs;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class Sender extends Agent
{
	private static final long serialVersionUID = 1L;
	private int index;

	@Override
	public void setup()
	{
		final Object[] args = getArguments();
		index = (Integer) args[0];
		final int numIter = (Integer) args[1];
		
		addBehaviour(new CyclicBehaviour() {
			private static final long serialVersionUID = 1L;
			private int iterIndex;
			private AID mainAid;

			@Override
			public void action()
			{
				ACLMessage msg = myAgent.receive();
				if (msg == null)
					block();
				else
				{
					if (msg.getPerformative() == ACLMessage.REQUEST)
					{
						mainAid = msg.getSender();
						iterIndex = 0;
						sendMsg(msg.getContent(), System.currentTimeMillis() + "");
					}
					else
					{
						++iterIndex;
						if (iterIndex < numIter)
							sendMsg(msg.getContent(), msg.getInReplyTo());
						else
						{
							long time = System.currentTimeMillis() - Long.parseLong(msg.getInReplyTo());
							time /= numIter;
							ACLMessage res = new ACLMessage(ACLMessage.INFORM);
							res.addReceiver(mainAid);
							res.setContent(time + "");
							send(res);
						}
					}
				}
			}
		});
	}
	
	private void sendMsg(String content, String ts)
	{
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setSender(getAID());
		AID r = new AID("R" + index, false);
		msg.addReceiver(r);
		msg.setContent(content);
		msg.setReplyWith(ts);
		send(msg);
	}
}
