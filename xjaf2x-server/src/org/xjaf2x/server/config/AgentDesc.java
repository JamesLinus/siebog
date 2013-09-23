package org.xjaf2x.server.config;

import java.io.Serializable;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.agentmanager.agent.jason.JasonAgentI;

/**
 * Description of a deployed agent.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public final class AgentDesc implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final String viewName = AgentI.class.getName();
	private static final String jasonViewName = JasonAgentI.class.getName();
	private final String family;
	private final boolean stateful;
	private final String jndiName;

	public AgentDesc(String family, boolean stateful, String appName, boolean isJason)
	{
		this.family = family;
		this.stateful = stateful;

		int n = family.lastIndexOf('.');
		String beanName = n < 0 ? family : family.substring(n + 1);
		
		final String view = isJason ? jasonViewName : viewName;
		final String str = String.format("ejb:/%s//%s!%s", appName, beanName, view);
		if (stateful)
			jndiName = str + "?stateful";
		else
			jndiName = str;
	}

	public String getJndiName()
	{
		return jndiName;
	}

	public String getFamily()
	{
		return family;
	}

	public boolean isStateful()
	{
		return stateful;
	}
}
