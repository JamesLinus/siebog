package org.xjaf2x.server;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.xjaf2x.server.agentmanager.AgentManager;
import org.xjaf2x.server.agentmanager.AgentManagerI;

public class JndiManager
{
	private static final Hashtable<String, Object> jndiProps = new Hashtable<>();

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
		final String str = "ejb:/" + Global.SERVER + "//" + AgentManager.class.getSimpleName()
				+ "!" + AgentManagerI.class.getName();
		return (AgentManagerI) getContext().lookup(str);
	}
}
