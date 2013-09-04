package org.xjaf2x.server.agentmanager;

import java.io.Serializable;
import java.util.Set;
import org.xjaf2x.server.agentmanager.acl.ACLMessage;
import org.xjaf2x.server.agentmanager.agent.AID;
import org.xjaf2x.server.agentmanager.agent.jason.JasonAgentI;

public interface AgentManagerI extends Serializable
{
	/**
	 * Runs a new instance of an agent.
	 * 
	 * @param family Agent family name.
	 * @param runtimeName Runtime name of this agent.
	 * @return AID instance on success, null otherwise.
	 */
	AID startAgent(String family, String runtimeName);
	
	JasonAgentI startJasonAgent(String family, String runtimeName);

	/**
	 * Terminates an active agent.
	 * 
	 * @param aid AID object.
	 */
	void stopAgent(AID aid);
	
	/**
	 * Posts an ACL message. Invocation is asynchronous: it will NOT wait for any of the agents to
	 * process the message.
	 * 
	 * @param message ACLMessage instance.
	 */
	void post(ACLMessage message);

	/**
	 * Returns the set of deployed agent families.
	 * 
	 * @param reload true if the list of deployed agents should be reloaded.
	 * @return
	 */
	Set<String> getFamilies();
}
