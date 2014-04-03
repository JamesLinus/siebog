package org.xjaf2x.server.agents.cnet;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.jboss.ejb3.annotation.Clustered;
import org.xjaf2x.server.agentmanager.agent.AID;
import org.xjaf2x.server.agentmanager.agent.Agent;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import org.xjaf2x.server.messagemanager.fipaacl.Performative;

@Stateful(name = "org_xjaf2x_server_agents_cnet_CNetManager")
@Remote(AgentI.class)
@Clustered
public class CNetManager extends Agent
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CNetManager.class.getName());
	private AID starter;
	private int numContr;
	private byte[] content;
	private long total;
	private int received;

	@Override
	public void onMessage(ACLMessage message)
	{
		switch (message.getPerformative())
		{
		case REQUEST:
			starter = message.getSender();
			
			String[] args = ((String) message.getContent()).split(" ");
			numContr = Integer.parseInt(args[0]);
			final int size = Integer.parseInt(args[1]);
			
			content = new byte[size];
			for (int i = 0; i < size; content[i++] = (byte) (Math.random() * 128))
				;

			total = 0;
			received = 0;
			sendCfps();
			break;
		case PROPOSE:
			ACLMessage accept = message.makeReply(Performative.ACCEPT_PROPOSAL);
			accept.setSender(getAid());
			accept.setContent(content);
			accept.setReplyWith(message.getInReplyTo());
			msgMngr().post(accept);
			break;
		case INFORM:
			long time = System.nanoTime() - Long.parseLong(message.getInReplyTo());
			total += time / 1000000L;
			++received;
			if (received == numContr)
			{
				total = total / numContr;
				if (logger.isLoggable(Level.INFO))
					logger.info(String.format("Average time per message: [%d ms]", total));
				// send results to receiver
				ACLMessage reply = new ACLMessage(Performative.INFORM);
				reply.setSender(getAid());
				reply.addReceiver(starter);
				reply.setContent(total);
				msgMngr().post(reply);
			}
			break;
		default:
			break;
		}
	}

	private void sendCfps()
	{
		if (logger.isLoggable(Level.INFO))
			logger.info("Sending CFPs to [" + numContr + "] contractors");
		new Thread() {
			@Override
			public void run()
			{
				for (int i = 0; i < numContr; i++)
				{
					ACLMessage cfp = new ACLMessage(Performative.CALL_FOR_PROPOSAL);
					cfp.setSender(getAid());
					cfp.addReceiver(new AID("C" + i, "org.xjaf2x.examples.cnet.CNetContractor"));
					cfp.setContent(content);
					cfp.setProtocol("fipa-contract-net");
					cfp.setLanguage("fipa-sl");
					cfp.setReplyWith(System.nanoTime() + "");
					msgMngr().post(cfp);
				}
			}
		}.start();
	}
}
