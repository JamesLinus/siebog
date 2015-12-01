package siebog.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import siebog.agents.AgentManagerBean;

/**
 * @author Nikola
 */
@ServerEndpoint("/console")
public class LoggerUtil {
	private static List<Session> sessions = new ArrayList<Session>();
	private static final Logger LOG = LoggerFactory.getLogger(AgentManagerBean.class);
	
	public static void log(String message) {
		log(message, false);
	}
	
    public static void log(String message, boolean websocket) {
    	LOG.info(message);
    	if(websocket) {
    		try {
    		    for(Session s : sessions) {
					s.getBasicRemote().sendText(message);
    		    }
    		} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
    @OnOpen
    public void register(Session session) {
       	sessions.add(session);
    }
}
