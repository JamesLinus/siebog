package xjaf2x.client.agents.pairs;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;
import javax.sound.midi.Receiver;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import xjaf2x.Global;
import xjaf2x.server.agentmanager.AID;
import xjaf2x.server.agentmanager.AgentManagerI;
import xjaf2x.server.agents.pairs.Sender;
import xjaf2x.server.config.Xjaf2xCluster;
import xjaf2x.server.messagemanager.MessageManagerI;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import xjaf2x.server.messagemanager.fipaacl.Performative;

public class Starter
{

	public static void main(String[] args) throws NamingException, IOException,
			ParserConfigurationException, SAXException
	{
		String addr = System.getProperty("java.rmi.server.hostname");
		if (args.length != 4 || addr == null)
		{
			System.out.println("I need 4 arguments: NumOfPairs NumIterations PrimeLimit MsgContentLen");
			System.out.println("In addition, set the property java.rmi.server.hostname to the address of this computer.");
			return;
		}

		int numPairs = Integer.parseInt(args[0]);
		int numIterations = Integer.parseInt(args[1]);
		int primeLimit = Integer.parseInt(args[2]);
		int contentLength = Integer.parseInt(args[3]);

		Xjaf2xCluster.init(true);

		List<AID> senders = new ArrayList<>();
		AgentManagerI agm = Global.getAgentManager();
		for (int i = 0; i < numPairs; i++)
		{
			// receiver
			AID aid = new AID(Global.SERVER, Global.getEjbName(Receiver.class), "R" + i);
			agm.start(aid, primeLimit, numIterations);
			// sender
			aid = new AID(Global.SERVER, Global.getEjbName(Sender.class), "S" + i);
			agm.start(aid, numIterations, contentLength, addr);
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
