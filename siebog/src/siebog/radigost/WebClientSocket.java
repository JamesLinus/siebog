package siebog.radigost;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.agents.AID;
import siebog.interaction.ACLMessage;

@ServerEndpoint("/webclient")
public class WebClientSocket {
	private static final Logger LOG = LoggerFactory.getLogger(WebClientSocket.class);
	// @formatter:off
	public static final char 
		MSG_REGISTER 	= 'r', 
		MSG_DEREGISTER 	= 'd', 
		MSG_GET_STATE	= 'g',
		MSG_STORE_STATE	= 's';
	//@formatter:on
	private static final Map<String, Session> sessions = Collections
			.synchronizedMap(new HashMap<String, Session>());
	@Inject
	private Cassandra cassandra;

	@OnOpen
	public void onOpen(Session session) {
		LOG.debug("WebSocket Open: {}.", session.getId());
	}

	@OnClose
	public void onClose(CloseReason closeReason) {
		LOG.debug("WebSocket Closed: {}.", closeReason.getReasonPhrase());
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		if (message == null || message.isEmpty())
			throw new IllegalArgumentException("Messages cannot be empty.");

		char cmd = message.charAt(0);
		String content = message.substring(1);
		switch (cmd) {
		case MSG_REGISTER:
			doRegister(content, session);
			break;
		case MSG_DEREGISTER:
			doUnregister(content, session);
			break;
		case MSG_GET_STATE:
			doGetState(content, session);
			break;
		case MSG_STORE_STATE:
			doStoreState(content);
			break;
		}
	}

	public void sendMessageToClient(@Observes @Default ACLMessage msg) {
		Set<String> processed = new HashSet<>();
		AID aid = msg.receivers.iterator().next();
		synchronized (sessions) {
			Session session = sessions.get(aid.getHost());
			if (session != null && processed.add(aid.getHost())) {
				session.getAsyncRemote().sendText(msg.toString());
			}
		}
	}

	private void doRegister(String platformId, Session session) {
		sessions.put(platformId, session);
	}

	private void doUnregister(String platformId, Session session) {
		sessions.remove(platformId);
	}

	private void doGetState(String aid, Session session) {
		String state = cassandra.getState(aid);
		session.getAsyncRemote().sendText(state);
	}

	private void doStoreState(String content) {
		int delimiter = getDelimiter(content);
		String aid = content.substring(0, delimiter);
		String state = getState(content, delimiter);
		cassandra.setState(aid, state);
	}

	private int getDelimiter(String content) {
		int delimiter = content.indexOf('$');
		if (delimiter <= 0)
			throw new IllegalArgumentException("AID cannot be empty.");
		return delimiter;
	}

	private String getState(String content, int delimiter) {
		return delimiter == content.length() - 1 ? "" : content.substring(delimiter + 1);
	}
}
