package org.xjaf2x.server.messagemanager;

import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;

public interface MessageManagerI
{
	/**
	 * Posts an ACL message. Invocation is asynchronous: it will NOT wait for any of the agents to
	 * process the message.
	 * 
	 * @param message ACLMessage instance.
	 */
	void post(ACLMessage message);
}
