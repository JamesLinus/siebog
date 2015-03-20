package siebog.radigost;

import javax.inject.Inject;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/sp")
public class StatePersistence2 {
	@Inject
	private Cassandra cassandra;

	@OnMessage
	public void onMessage(String message, Session session) {
		if (message == null || message.length() < 2)
			throw new IllegalArgumentException("Cannot process empty messages.");
		String content = message.substring(1);
		if (isRead(message)) {
			doRead(content, session);
		} else {
			doUpdate(content);
		}
	}

	private boolean isRead(String message) {
		return message.charAt(0) == 'R';
	}

	private void doRead(String aid, Session session) {
		String state = cassandra.getState(aid);
		session.getAsyncRemote().sendText(state);
	}

	private void doUpdate(String content) {
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
