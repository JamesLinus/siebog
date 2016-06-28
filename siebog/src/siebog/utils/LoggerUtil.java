package siebog.utils;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.hornetq.utils.json.JSONException;
import org.hornetq.utils.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import siebog.agentmanager.AID;
import siebog.agentmanager.AgentManagerBean;
import siebog.messagemanager.ACLMessage;

/**
 * @author Nikola
 */
@ServerEndpoint("/console")
public class LoggerUtil {
	private static List<Session> sessions = new ArrayList<Session>();
	private static final Logger LOG = LoggerFactory.getLogger(AgentManagerBean.class);
	
	public enum SocketMessageType {LOG, ADD, REMOVE};
	
	public static void log(String message) {
		log(message, false);
	}
	
    public static void log(String message, boolean websocket) {
    	LOG.info(message);
    	if(websocket) {
    		try {
    			JSONObject obj = new JSONObject();
		    	obj.put("type", SocketMessageType.LOG);
		    	obj.put("data", message);
    		    for(Session s : sessions) {
    		    	try {
    		    		s.getBasicRemote().sendText(obj.toString());
    		    	} catch(Exception e) {
    		    		LOG.error(e.getMessage());
    		    	}
    		    }
    		} catch (JSONException e) {
    			LOG.error(e.getMessage());
			}
    	}
    }
    
    /**
     * This function updates the angular UI running server agents list.
     * A stopped agent will be removed from the list, while a newly started 
     * one will get added to the list. 
     * @param agent - the agent that's being stopped or started
     * @param type  - the type of operation (add or remove) 
     */
    public static void logAgent(AID agent, SocketMessageType type) {
    	try {
    		JSONObject obj = new JSONObject();
	    	obj.put("type", type);
	    	obj.put("data", agent);
		    for(Session s : sessions) {
		    	try {
		    		s.getBasicRemote().sendText(obj.toString());
		    	} catch(Exception e) {
		    		LOG.error(e.getMessage());
		    	}
		    }
		} catch (JSONException e) {
			LOG.error(e.getMessage());
		}
    }
    
    @OnOpen
    public void register(Session session) {
       	sessions.add(session);
       	LOG.info("Socket opened: " + session.getId());
    }
    
    @OnClose
    public void unRegister(Session session) {
    	for(int i = 0; i < sessions.size(); i++) {
    		if(session.getId().equals(sessions.get(i).getId())) {
    			sessions.remove(i);
    			LOG.info("Socket closed: " + session.getId());
    			break;
    		}
    	}
    }

	public static void logMessage(ACLMessage msg, AID myAid) {
		log("PERFORMATIVE: " + msg.performative + "\tFROM: " + (msg.sender == null ? "unknown" : msg.sender.getStr()) + "\tTO: " + (myAid == null? "unknown" : myAid.getStr())+ "\tCONTENT: " + msg.content, true);
	}
}
