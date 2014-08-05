package siebog.server.radigost.websocket;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import siebog.server.radigost.Global;

@ServerEndpoint("/websocket")
public class WebSocketEndpoint
{
	private static final Logger logger = Logger.getLogger(WebSocketEndpoint.class.getName());
	
	@OnMessage
	public void onMessage(String message, Session client)
	{
		if ((message == null) || (message.length() == 0))
			return;

		// commands are in the form of 0xFFnnn=vvv where where "nnn" is a
		// 3-letter name and "vvv" is an arbitrary value
		try
		{
			if (message.charAt(0) == 0xff)
			{
				final String name = message.substring(1, 4);
				final String value = message.substring(5);
				Global.getBridgeManager().onRadigostCommand(client, name, value);
			}
			else
				Global.getBridgeManager().onMessageFromRadigost(message);
		} catch (NamingException ex)
		{
			logger.log(Level.WARNING, "Unable to process incoming Websocket message.", ex);
		}
	}
}
