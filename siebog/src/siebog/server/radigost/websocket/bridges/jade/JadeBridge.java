package siebog.server.radigost.websocket.bridges.jade;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import siebog.server.radigost.websocket.bridges.AbstractBridge;
import siebog.server.radigost.websocket.bridges.BridgeException;

/**
 * A bridge between Radigost and JADE. Enables transparent communication between agents deployed in
 * the two systems.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 * @see radigost.server.websocket.bridges.AbstractBridge
 */
public class JadeBridge extends AbstractBridge
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(JadeBridge.class.getName());
	public static final String NAME = "JadeBridge";
	private AgentController radigostAgent;
	private AgentContainer agentContainer;

	public JadeBridge(String host) throws BridgeException
	{
		Properties p = new Properties();
		// start the JADE container
		int n = host.lastIndexOf(':');
		String port = "1099";
		if (n > 0)
		{
			port = host.substring(n + 1);
			host = host.substring(0, n);
		}
		p.setProperty(Profile.LOCAL_HOST, host);
		p.setProperty(Profile.LOCAL_PORT, port);
		try
		{
			agentContainer = Runtime.instance().createMainContainer(new ProfileImpl(p));
			radigostAgent = agentContainer.acceptNewAgent("radigostAgent", new RadigostAgent(this));
			radigostAgent.start();

			// TODO: fix this
			// agentContainer.acceptNewAgent("manager", new Manager()).start();
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "Cannot start JadeBridge", ex);
			throw new BridgeException(ex);
		}
	}

	@Override
	public void destroy()
	{
		try
		{
			if (radigostAgent != null)
				try
				{
					radigostAgent.kill();
				} catch (Exception ex)
				{
				}
			if (agentContainer != null)
				try
				{
					agentContainer.kill();
				} catch (Exception ex)
				{
				}
		} finally
		{
			agentContainer = null;
			radigostAgent = null;
		}
	}

	/**
	 * Called when a Radigost agent sends a message to a JADE agent. The method constructs a
	 * jade.lang.acl.ACLMessage object and forwards it to the target agent.
	 * 
	 * @param msg JSON-formatted FIPA ACL message.
	 */
	@Override
	public void onMessageFromRadigost(String msg)
	{
		try
		{
			ACLMessage acl = JsonAclCodec.decode(msg);
			radigostAgent.putO2AObject(acl, AgentController.ASYNC);
		} catch (Exception ex)
		{
			logger.log(Level.WARNING, "Error while processing received message\n" + msg + "\n", ex);
		}
	}

	/**
	 * Called when a JADE agent sends a message to a Radigost agent. The method turns the message
	 * into a JSON-formatted string and forwards it to the target agent.
	 * 
	 * @param jade.lang.acl.ACLMessage object.
	 */
	@Override
	public void postMessage(Serializable msg)
	{
		ACLMessage acl = (ACLMessage) msg;
		List<String> aids = new ArrayList<>();
		try
		{
			String str = JsonAclCodec.encode(acl, aids);
			doPost(str, aids);
		} catch (Exception ex)
		{
			logger.log(Level.WARNING, "Error while encoding ACLMessage to JSON string", ex);
		}
	}
}
