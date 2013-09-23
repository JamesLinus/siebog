package org.xjaf2x.server;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.xjaf2x.server.agentmanager.AgentManager;
import org.xjaf2x.server.agentmanager.AgentManagerI;
import org.xjaf2x.server.messagemanager.MessageManager;
import org.xjaf2x.server.messagemanager.MessageManagerI;

public class JndiManager
{
	private static final Hashtable<String, Object> jndiProps = new Hashtable<>();
	private static final String AgentManagerLookup = "ejb:/" + Global.SERVER + "//"
			+ AgentManager.class.getSimpleName() + "!" + AgentManagerI.class.getName();
	private static final String MessageManagerLookup = "ejb:/" + Global.SERVER + "//"
			+ MessageManager.class.getSimpleName() + "!" + MessageManagerI.class.getName();

	static
	{
		jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
	}

	public static Context getContext() throws Exception
	{
		return new InitialContext(jndiProps);
	}

	public static AgentManagerI getAgentManager() throws Exception
	{
		return (AgentManagerI) getContext().lookup(AgentManagerLookup);
	}
	
	public static MessageManagerI getMessageManager() throws Exception
	{
		return (MessageManagerI) getContext().lookup(MessageManagerLookup);
	}
}
