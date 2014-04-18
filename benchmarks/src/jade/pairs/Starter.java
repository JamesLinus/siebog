package jade.pairs;

import java.util.ArrayList;
import java.util.List;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

public class Starter extends Agent
{
	private static final long serialVersionUID = -3148135523727782805L;
	
	
	@Override
	public void setup()
	{		
		addBehaviour(new OneShotBehaviour() {
			private static final long serialVersionUID = -6113426687347387718L;

			@Override
			public void action()
			{
				String[] args = getArguments()[0].toString().split("-");
				int numPairs = Integer.parseInt(args[0]);
				int numIterations = Integer.parseInt(args[1]);
				int primeLimit = Integer.parseInt(args[2]);
				int contentLength = Integer.parseInt(args[3]);
				
				// create containers
				jade.core.Runtime rt = jade.core.Runtime.instance();
				List<AgentContainer> containers = new ArrayList<>(); 
				containers.add(getContainerController());
				for (int i = 4; i < args.length; i++)
				{
					Profile pp = new ProfileImpl(args[i], 2099, "ac" + args[i], false);
					AgentContainer ac = rt.createAgentContainer(pp);
					containers.add(ac);
				}
				int contIndex = 0;
				final int maxContIndex = containers.size() - 1;
				
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.setContent(makeContent(contentLength));
				
				final Object[] rargs = { primeLimit, numIterations };
				try
				{
					for (int i = 0; i < numPairs; i++)
					{
						// next container
						AgentContainer ac = containers.get(contIndex);
						if (contIndex == maxContIndex)
							contIndex = 0;
						else
							++contIndex;
								
						// run agents
						ac.createNewAgent("R" + i, Receiver.class.getName(), rargs).start();
						Object[] sargs = { i, numIterations };
						String nick = "S" + i;
						ac.createNewAgent(nick, Sender.class.getName(), sargs).start();
						msg.addReceiver(new AID(nick, false));
					}
				} catch (StaleProxyException e)
				{
					e.printStackTrace();
				}
				
				send(msg);
			}
		});
	}
	
	private String makeContent(int length)
	{
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++)
			sb.append("A");
		return sb.toString();
	}
}
