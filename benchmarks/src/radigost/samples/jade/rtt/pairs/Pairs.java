package radigost.samples.jade.rtt.pairs;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;

public class Pairs extends Agent
{
	private static final long serialVersionUID = 1L;
	private int[] pairs;
	private int numIter;
	private int contLen;
	private int primeLimit;
	private int pairIndex;
	private int total;
	private int received;
	private Set<String> existing;
	private ACLMessage msg;
	private String msgCont;

	@Override
	public void setup()
	{
		existing = new HashSet<>();
		msg = new ACLMessage(ACLMessage.REQUEST);
		if (!extractArgs())
			return;

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
					total += Integer.parseInt(msg.getContent());
					++received;
					if (received == pairs[pairIndex])
						new Timer().schedule(new TimerTask() {
							@Override
							public void run()
							{
								nextPair();
							}
						}, 100);
				}
			}
		});

		pairIndex = -1;
		msgCont = "";
		for (int i = 0; i < contLen; i++)
			msgCont += "A";
		total = 0;
		nextPair();
	}

	private void nextPair()
	{
		if (pairIndex >= 0)
		{
			total /= pairs[pairIndex];
			System.out.println(total);
			total = 0;
		}
		++pairIndex;
		if (pairIndex >= pairs.length)
			System.out.println("Done.");
		else
		{
			createAgents();
			received = 0;
			send(msg);
			System.out.println("=== Pairs: " + pairs[pairIndex]);
		}
	}

	private boolean extractArgs()
	{
		Object[] args = getArguments();
		if (args.length != 4)
		{
			System.out.println("args:\n\tpairList numIter contLen primeLimit");
			return false;
		}

		String[] pl = ((String) args[0]).split("-");
		pairs = new int[pl.length];
		for (int i = 0; i < pl.length; i++)
			pairs[i] = Integer.parseInt(pl[i]);

		numIter = Integer.parseInt((String) args[1]);
		contLen = Integer.parseInt((String) args[2]);
		primeLimit = Integer.parseInt((String) args[3]);

		return true;
	}

	private void createAgents()
	{
		try
		{
			msg.setContent(msgCont);
			msg.clearAllReceiver();
			for (int i = 0; i < pairs[pairIndex]; i++)
			{
				String nick = "R" + i;
				if (existing.add(nick))
				{
					Object[] args = new Object[] { primeLimit };
					AgentController ag = getContainerController().createNewAgent(nick,
							Receiver.class.getName(), args);
					ag.start();
				}

				nick = "S" + i;
				AID aid = new AID(nick, false);
				if (existing.add(aid.getName()))
				{
					Object[] args = new Object[] { i, numIter };
					AgentController ag = getContainerController().createNewAgent(nick,
							Sender.class.getName(), args);
					ag.start();
				}
				msg.addReceiver(aid);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
