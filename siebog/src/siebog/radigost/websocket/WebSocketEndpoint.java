package siebog.radigost.websocket;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import siebog.utils.ObjectFactory;

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
			ObjectFactory.getBridgeManager().onRadigostCommand(client, name, value);
		} else
			ObjectFactory.getBridgeManager().onMessageFromRadigost(message);
	}
}
