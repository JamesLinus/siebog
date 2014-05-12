package jade.pairs;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.KillAgent;
import jade.lang.acl.ACLMessage;

public class Sender extends Agent
{
	private static final long serialVersionUID = -1973969989601185912L;
	private static final Logger logger = Logger.getLogger(Sender.class.getName());
	private int myIndex;
	private int numIterations;
	private int iterationIndex;
	private AID receiver;
	private String content;
	private String resultsServiceAddr;
	private long totalTime;
	
	@Override
	protected void setup()
	{
		myIndex = Integer.parseInt(getArguments()[0].toString());
		receiver = new AID("R" + myIndex, true);
		numIterations = Integer.parseInt(getArguments()[1].toString());
		// create content
		int contentLength = Integer.parseInt(getArguments()[2].toString());
		content = makeContent(contentLength);
		resultsServiceAddr = getArguments()[3].toString();
		
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
						totalTime = 0;
						postMsg();
					}
					else
					{
						++iterationIndex;
						totalTime += System.currentTimeMillis() - Long.parseLong(msg.getInReplyTo());
						if (iterationIndex < numIterations)
							postMsg();
						else
						{
							long avg = totalTime / numIterations;
							try
							{
								Registry reg = LocateRegistry.getRegistry(resultsServiceAddr, 2099);
								ResultsServiceI results = (ResultsServiceI) reg.lookup("ResultsService");
								results.add(avg);
							} catch (RemoteException | NotBoundException ex)
							{
								logger.log(Level.SEVERE, "Cannot connect to ResultsService.", ex);
							} finally
							{
								myAgent.doDelete();
							}
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
	
	private void postMsg()
	{
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setSender(getAID());
		msg.addReceiver(receiver);
		msg.setContent(content);
		msg.setReplyWith(System.currentTimeMillis() + "");
		send(msg);		
	}
	
	private String makeContent(int length)
	{
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++)
			sb.append("A");
		return sb.toString();
	}
	
	@SuppressWarnings("unused")
	private void kill(AID aid)
	{
		// taken from http://avalon.tilab.com/pipermail/jade-develop/2005q4/007805.html
		KillAgent ka = new KillAgent();
		ka.setAgent(aid);
		Action action = new Action(getAMS(), ka);
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.setLanguage(new SLCodec().getName());
		request.setOntology(JADEManagementOntology.NAME);
		try
		{
			getContentManager().fillContent(request, action);
			request.addReceiver(action.getActor());
			send(request);
		} catch (Exception ex)
		{
			logger.log(Level.WARNING, "Error while killing agent " + aid, ex);
		}
	}
}
