package org.xjaf2x.server.agentmanager.agent;

import java.io.Serializable;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;

/**
 * Base interface for all agents.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public interface AgentI extends Serializable
{
	void init();
	
	void terminate();
	
	/**
	 * Called once there is a message in the agent's message queue.
	 * 
	 * @param message ACLMessage instance.
	 */
	void onMessage(ACLMessage message);
	
	AID getAid();
	
	void setAid(AID aid);
}
