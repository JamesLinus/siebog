package siebog.xjaf.managers.webclient;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import siebog.PlatformId;
import siebog.xjaf.core.AID;
import siebog.xjaf.fipa.ACLMessage;

@ServerEndpoint("/webclient")
public class WebClientSocket {
	private static final Logger logger = Logger.getLogger(WebClientSocket.class.getName());
	// @formatter:off
	public static final char 	MSG_REGISTER 	= 'r', 
								MSG_DEREGISTER 	= 'd', 
								MSG_NEW_AGENT 	= 'a';
	//@formatter:on
	private static final Map<String, Session> sessions = Collections.synchronizedMap(new HashMap<String, Session>());

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
		if (message == null || message.isEmpty())
			throw new IllegalArgumentException("Messages cannot be empty.");

		char cmd = message.charAt(0);
		switch (cmd) {
		case MSG_REGISTER:
			String regId = message.substring(1);
			sessions.put(regId, session);
			break;
		case MSG_DEREGISTER:
			String unregId = message.substring(1);
			sessions.remove(unregId);
			break;
		}
	}

	public void sendMessageToClient(@Observes @Default ACLMessage msg) {
		Set<String> processed = new HashSet<>();
		for (AID aid : msg.receivers)
			if (aid.getPid() == PlatformId.RADIGOST) {
				String host = aid.getHost();
				Session session = sessions.get(host);
				if (session != null && processed.add(host))
					session.getAsyncRemote().sendText(msg.toString());
			}
	}
}
