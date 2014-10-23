package siebog.xjaf.managers;

import java.util.logging.Logger;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import siebog.utils.ObjectFactory;

@ServerEndpoint("/webclient")
public class WebClientSocket {
	private static final Logger logger = Logger.getLogger(WebClientSocket.class.getName());
	// @formatter:off
	public static final char 	MSG_REGISTER 	= 'r', 
								MSG_DEREGISTER 	= 'd', 
								MSG_NEW_AGENT 	= 'a';
	//@formatter:on
	private WebClientManager webClientManager;

	public WebClientSocket() {
		webClientManager = ObjectFactory.getWebClientManager();
	}

	@OnOpen
	public void onOpen(Session session) {
		logger.info("Open: " + session.getId());
	}

	@OnClose
	public void onClose(CloseReason closeReason) {
		logger.info("Closed: " + closeReason.getReasonPhrase());
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		logger.warning("Message: [" + message + "]");
		if (message == null || message.isEmpty())
			throw new IllegalArgumentException("Messages cannot be empty.");

		char cmd = message.charAt(0);
		switch (cmd) {
		case MSG_REGISTER:
			try {
				String id = message.substring(1);
				// TODO WebSocket Session cannot be serialized.
				// webClientManager.onWebClientRegistered(id, session);
				logger.info("New web client: " + id);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			break;
		case MSG_DEREGISTER:
			webClientManager.onWebClientDeregistered(message.substring(1));
			break;
		}
	}
}
