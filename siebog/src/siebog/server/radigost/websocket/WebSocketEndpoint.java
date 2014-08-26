package siebog.server.radigost.websocket;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import siebog.server.utils.ManagerFactory;

@ServerEndpoint("/websocket")
public class WebSocketEndpoint {
	@OnMessage
	public void onMessage(String message, Session client) {
		if ((message == null) || (message.length() == 0))
			return;

		// commands are in the form of 0xFFnnn=vvv where where "nnn" is a
		// 3-letter name and "vvv" is an arbitrary value
		if (message.charAt(0) == 0xff) {
			final String name = message.substring(1, 4);
			final String value = message.substring(5);
			ManagerFactory.getBridgeManager().onRadigostCommand(client, name, value);
		} else
			ManagerFactory.getBridgeManager().onMessageFromRadigost(message);
	}
}
