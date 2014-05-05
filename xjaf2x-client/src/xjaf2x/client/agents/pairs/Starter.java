package xjaf2x.client.agents.pairs;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import xjaf2x.server.Global;
import xjaf2x.server.agentmanager.AID;
import xjaf2x.server.agentmanager.AgentManagerI;
import xjaf2x.server.config.Xjaf2xCluster;
import xjaf2x.server.messagemanager.MessageManagerI;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import xjaf2x.server.messagemanager.fipaacl.Performative;

public class Starter
{

	public static void main(String[] args) throws NamingException, IOException,
			ParserConfigurationException, SAXException
	{
		if (args.length != 4)
		{
			System.out.println("Expecting the following program arguments:");
			System.out.println("\tNumOfPairs NumIterations PrimeLimit MsgContentLen");
			return;
		}

		int numPairs = Integer.parseInt(args[0]);
		int numIterations = Integer.parseInt(args[1]);
		int primeLimit = Integer.parseInt(args[2]);
		int contentLength = Integer.parseInt(args[3]);

		String addr = System.getProperty("java.rmi.server.hostname");
		if (addr == null)
		{
			System.out.println("VM argument java.rmi.server.hostname not defined, using localhost");
			addr = "localhost";
		}
		
		Xjaf2xCluster.init(true);
		

		List<AID> senders = new ArrayList<>();
		AgentManagerI agm = Global.getAgentManager();
		final String family = "xjaf2x_server_agents_pairs_Sender";
		for (int i = 0; i < numPairs; i++)
		{
			AID aid = agm.start(family, "S" + i, i, numIterations, primeLimit, contentLength, addr);
			senders.add(aid);
		}

		Registry reg = LocateRegistry.createRegistry(1099);
		reg.rebind("ResultsService", new ResultsServiceImpl(numPairs));

		MessageManagerI msm = Global.getMessageManager();
		for (AID aid : senders)
		{
			ACLMessage msg = new ACLMessage(Performative.REQUEST);
			msg.addReceiver(aid);
			msm.post(msg);
		}
	}

}
