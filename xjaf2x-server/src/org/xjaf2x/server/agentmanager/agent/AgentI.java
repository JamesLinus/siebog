package org.xjaf2x.server.agentmanager.agent;

import java.io.Serializable;
import javax.ejb.Remote;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;

/**
 * Base interface for all agents.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Remote
public interface AgentI extends Serializable
{
	void init(Serializable[] args);

	void terminate();

	AID getAid();

	void setAid(AID aid);

	/**
	 * The remaining methods are for internal purposes only. You should never directly call or
	 * override any of them.
	 */

	void handleMessage(ACLMessage msg);
	void processNextMessage();
}
