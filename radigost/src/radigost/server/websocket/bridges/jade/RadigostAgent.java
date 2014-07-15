package radigost.server.websocket.bridges.jade;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;

public class RadigostAgent extends Agent
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(RadigostAgent.class.getName());
	private JadeBridge jadeBridge;
	// radigost name -> jade name (the jade name has a platform id appended)
	private Map<String, String> radigostNames;
	private static AID myaid;

	public RadigostAgent(JadeBridge jadeBridge)
	{
		this.jadeBridge = jadeBridge;
		radigostNames = new HashMap<>();
		setEnabledO2ACommunication(true, 0);
	}

	@Override
	protected void setup()
	{
		myaid = getAID();

		// Radigost --> Jade messages
		addBehaviour(new CyclicBehaviour() {
			private static final long serialVersionUID = 1L;

			@Override
			public void action()
			{
				Object obj = getO2AObject();
				if (obj == null)
					block();
				else
				{
					ACLMessage msg = (ACLMessage) obj;

					// register this agent under the names given in 'sender',
					// and 'replyTo' so that any replies end up here
					AID sender = msg.getSender();
					if (sender != null)
					{
						String newName = register(sender);
						sender.setName(newName);
					}
					// reply-to's
					Iterator<?> i = msg.getAllReplyTo();
					while (i.hasNext())
					{
						AID aid = (AID) i.next();
						String newName = register(aid);
						aid.setName(newName);
					}
					
					// ok, forward the message
					send(msg);
				}
			}
		});

		// Jade --> Radigost messages
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
					// remove the JADE platform identifier 
					Iterator<?> i = msg.getAllReceiver();
					while (i.hasNext())
					{
						AID aid = (AID) i.next();
						final String localName = aid.getLocalName();
						if (radigostNames.containsKey(localName))
							aid.setName(aid.getLocalName());
					}
					
					// go
					jadeBridge.postMessage(msg);
				}
			}
		});
	}

	@Override
	public void takeDown()
	{
		// TODO : remove all registered names
	}

	private String register(AID aid)
	{
		try
		{
			String newName = radigostNames.get(aid.getName());
			if (newName == null)
			{
				AgentController ac = getContainerController().acceptNewAgent(aid.getName(), this);
				newName = ac.getName();
				radigostNames.put(aid.getName(), newName);
			}
			return newName;
		} catch (Exception ex)
		{
			logger.log(Level.WARNING, "Unable to register RadigostAgent as " + aid, ex);
			return null;
		}
	}

	public static AID getGlobalAid()
	{
		return myaid;
	}
}
