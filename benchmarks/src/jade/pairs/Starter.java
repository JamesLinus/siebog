package jade.pairs;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

public class Starter extends Agent
{
	private static final long serialVersionUID = -3148135523727782805L;
	private int numPairs;

	@Override
	public void setup()
	{
		String[] args = getArguments()[0].toString().split("-");
		if (args.length != 7)
		{
			System.out.println("I need 7 arguments: NumOfNodes ZeroBasedIndexOfThisNode "
					+ "NumOfPairs NumIterations PrimeLimit MsgContentLen ReceiversServiceAddr");
			doDelete();
			return;
		}
		
		int numNodes = Integer.parseInt(args[0]);
		int nodeIndex = Integer.parseInt(args[1]);
		numPairs = Integer.parseInt(args[2]);
		int numIterations = Integer.parseInt(args[3]);
		int primeLimit = Integer.parseInt(args[4]);
		int contentLen = Integer.parseInt(args[5]);
		String rServiceAddr = args[6];
		
		int myNumPairs = numPairs / numNodes;
		Object[] receiverArgs = { primeLimit, numIterations };
		AgentContainer ac = getContainerController();
		try
		{
			for (int i = 0; i < myNumPairs; i++)
			{
				int index = i * numNodes + nodeIndex;
				ac.createNewAgent("R" + index, Receiver.class.getName(), receiverArgs).start();
				// sender
				Object[] senderArgs = { index, numIterations, contentLen, rServiceAddr };
				String nick = "S" + index;
				ac.createNewAgent(nick, Sender.class.getName(), senderArgs).start();
			}
		} catch (StaleProxyException ex)
		{
			ex.printStackTrace();
			return;
		}
		
		String addr = System.getProperty("java.rmi.server.hostname");
		if (addr == null)
			System.out.println("java.rmi.server.hostname not defined, this is not the main node.");
		else
		{
			try
			{
				Registry reg = LocateRegistry.createRegistry(2099);
				reg.rebind("ResultsService", new ResultsServiceImpl(numPairs));
			} catch (RemoteException ex)
			{
				ex.printStackTrace();
				return;
			}
		}
		
		addBehaviour(new OneShotBehaviour() {
			private static final long serialVersionUID = 1L;

			@Override
			public void action()
			{
				blockingReceive();
				for (int i = 0; i < numPairs; i++)
				{
					ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
					AID s = new AID("S" + i, false);
					request.addReceiver(s);
					send(request);
				}
			}
		});
	}
}
