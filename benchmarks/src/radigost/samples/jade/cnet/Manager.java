package radigost.samples.jade.cnet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

public class Manager extends Agent
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Manager.class.getName());
	private List<AID> receivers;
	private int informsPending;
	private long totalTime;
	private String msgContent;
	private int hash;
	private AID resultSender;

	@Override
	protected void setup()
	{
		receivers = new ArrayList<>();
		
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
					switch (msg.getPerformative())
					{
					case ACLMessage.REQUEST:
						char cmd = msg.getContent().charAt(0);
						switch (cmd)
						{
						case 'r': // reset all
							receivers.clear();
							informsPending = 0;
							totalTime = 0;
							if (logger.isLoggable(Level.INFO))
								logger.info("Reset all");
							break;
						case 'a': // add receivers
							addReceivers(msg.getContent().substring(1));
							if (logger.isLoggable(Level.INFO))
								logger.info("Receivers added, total number: " + receivers.size());
							break;
						case 'x': // execute
							resultSender = msg.getSender();
							informsPending = receivers.size();
							int msgLen = Integer.parseInt(msg.getContent().substring(1));
							msgContent = getMsgContent(msgLen);
							if (logger.isLoggable(Level.INFO))
								logger.info("Sending " + receivers.size() + " CFPs, message length: " + msgLen);
							sendCfps();
							break;
						}
						break;
					case ACLMessage.PROPOSE:
						checkHash(msg);
						
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
						reply.setReplyWith(msg.getInReplyTo());
						reply.setContent(msgContent);
						send(reply);
						break;
					case ACLMessage.INFORM:
						checkHash(msg);
						
						totalTime += System.currentTimeMillis() - Long.parseLong(msg.getInReplyTo());
						if (--informsPending == 0)
						{
							long rtt = totalTime / receivers.size();
							if (logger.isLoggable(Level.INFO))
								logger.info("RTT: " + rtt + " ms");
							
							if ((resultSender != null) && !resultSender.equals(myAgent.getAID()))
							{
								ACLMessage res = new ACLMessage(ACLMessage.INFORM);
								res.addReceiver(resultSender);
								res.setContent(rtt + "");
								send(res);
							}
							
							informsPending = 0;
							totalTime = 0;
						}
						break;
					}
				}
			}
		});
		
		if (logger.isLoggable(Level.INFO))
			logger.info(getAID() + " running");
	}
	
	private void addReceivers(String content)
	{
		// a-b@device1,c-d@device2,...
		String[] args = content.split(",");
		for (String arg: args)
		{
			int dash = arg.indexOf('-');
			int monkey = arg.indexOf('@');
			int a = Integer.parseInt(arg.substring(0, dash));
			int b = Integer.parseInt(arg.substring(dash + 1, monkey));
			String device = arg.substring(monkey + 1);
			for (int i = a; i < b; i++)
			{
				AID aid = new AID("Contractor" + i + "@" + device, true);
				receivers.add(aid);
			}
		}
	}
	
	private String getMsgContent(int len)
	{
		hash = 0;
		StringBuilder str = new StringBuilder(len);
		for (int i = 0; i < len; i++)
		{
			int code = (int)(Math.random() * 26) + 97;
			hash += code;
			str.append((char) code);
		}
		return str.toString();
	}
	
	private void sendCfps()
	{
		new Thread() {
			@Override
			public void run()
			{
				for (AID aid : receivers)
				{
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
					cfp.setSender(getAID());
					// TODO: fix this
					//cfp.addReceiver(RadigostAgent.getGlobalAid());
					cfp.addReceiver(aid);
					cfp.setReplyWith(System.currentTimeMillis() + "");
					cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
					cfp.setContent(msgContent);
					send(cfp);
				}
			}
		}.start();
	}
	
	private void checkHash(ACLMessage msg)
	{
		if (hash != Integer.parseInt(msg.getContent()))
			logger.warning("Invalid hash from " + msg.getSender());
	}
	
	public void go(String agents, int msgLen)
	{
		addReceivers(agents);
		
		// go
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.addReceiver(getAID());
		msg.setContent("x" + msgLen);
		send(msg);
	}
}
