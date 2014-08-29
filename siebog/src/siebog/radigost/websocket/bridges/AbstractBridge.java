package siebog.radigost.websocket.bridges;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.websocket.Session;

/**
 * Base class that should be extended by every Radigost bridge.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class AbstractBridge implements Serializable
{
	private static final long serialVersionUID = 1L;
	private Map<String, Session> radigost2socket;
	private Map<String, Session> agent2socket;

	public AbstractBridge() throws BridgeException
	{
		radigost2socket = new HashMap<>();
		agent2socket = new HashMap<>();
	}

	/**
	 * Called upon receiving a FIPA ACL message from a Radigost instance.
	 * 
	 * @param msg JSON-formatted message.
	 */
	public abstract void onMessageFromRadigost(String msg);

	/**
	 * Called upon receiving a command from a Radigost instance.
	 * 
	 * @param client javax.websocket.Session object.
	 * @param name Command name.
	 * @param value Command value.
	 */
	public void onRadigostCommand(Session client, String name, String value)
	{
		switch (name)
		{
		case "run": // new Radigost instance, value is its id
			radigost2socket.put(value, client);
			break;
		case "ag+": // new agent, value is aid
			agent2socket.put(value, client);
			break;
		case "ag-": // agent done, value is aid
			agent2socket.remove(value);
			break;
		}
	}

	/**
	 * Posts a FIPA ACL message to Radigost agents.
	 * 
	 * @param msg
	 */
	public abstract void postMessage(Serializable msg);

	/**
	 * Performs the actual sending of a FIPA ACL message to Radigost agents on the client side.
	 * Usually called at the end of {@link #postMessage(Serializable)} in sub-classes.
	 * 
	 * @param msg JSON-formatted message.
	 * @param aids List of target WebSockets.
	 */
	protected void doPost(String msg, List<String> aids)
	{
		Set<Session> sent = new HashSet<>(aids.size());
		for (String aid : aids)
		{
			Session client = agent2socket.get(aid);
			if ((client != null) && sent.add(client))
				client.getAsyncRemote().sendText(msg);
		}
	}

	/**
	 * Called when the application is terminating. The bridge object should release its resources
	 * here.
	 */
	public abstract void destroy();
}
