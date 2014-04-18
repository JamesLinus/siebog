package jade.pairs;

import java.util.logging.Logger;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public class Sender extends Agent
{
	private static final long serialVersionUID = -1973969989601185912L;
	private static final Logger logger = Logger.getLogger(Sender.class.getName());
	private int myIndex;
	private int numIterations;
	private int iterationIndex;
	private String content;
	private AID receiver;
	
	@Override
	protected void setup()
	{
		myIndex = Integer.parseInt(getArguments()[0].toString());
		numIterations = Integer.parseInt(getArguments()[1].toString());
		receiver = new AID("R" + myIndex, true);
		
		addBehaviour(new Behaviour() {
			private static final long serialVersionUID = -2652088909934832736L;

			@Override
			public void action()
			{
				ACLMessage msg = receive();
				if (msg != null)
				{
					if (msg.getPerformative() == ACLMessage.REQUEST)
					{
						iterationIndex = 0;
						String time = "" + System.currentTimeMillis();
						content = msg.getContent();
						postMsg(time);
					}
					else
					{
						if (++iterationIndex < numIterations)
							postMsg(msg.getInReplyTo());
						else
						{
							long avg = System.currentTimeMillis() - Long.parseLong(msg.getInReplyTo());
							avg /= numIterations;
							logger.warning("S" + myIndex + ": " + avg);
						}
					}
				}
				else
					block();
			}
			
			@Override
			public boolean done()
			{
				return iterationIndex >= numIterations;
			}
		});
	}
	
	private void postMsg(String time)
	{
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setSender(getAID());
		msg.addReceiver(receiver);
		msg.setContent(content);
		msg.setReplyWith(time);
		send(msg);		
	}

}
