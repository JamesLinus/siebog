package org.xjaf2x.server.agentmanager.agent;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.xjaf2x.server.JndiManager;
import org.xjaf2x.server.agentmanager.AgentManagerI;
import org.xjaf2x.server.messagemanager.MessageManagerI;

/**
 * Adapter agent class, provides default implementations for some methods of
 * {@link AgentI}.
 * 
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class AgentAdapter implements AgentI
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AgentAdapter.class.getName());
	private AID aid;
	protected AgentManagerI agentManager;
	protected MessageManagerI messageManager;

	public AgentAdapter()
	{
		try
		{
			agentManager = JndiManager.getAgentManager();
			messageManager = JndiManager.getMessageManager();
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "Unable to initialize the base Agent class", ex);
		}
	}

	public void init()
	{
	}

	public void terminate()
	{
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aid == null) ? 0 : aid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgentAdapter other = (AgentAdapter) obj;
		if (aid == null)
		{
			if (other.aid != null)
				return false;
		} else if (!aid.equals(other.aid))
			return false;
		return true;
	}

	public AID getAid()
	{
		return aid;
	}

	public void setAid(AID aid)
	{
		this.aid = aid;
	}
}
