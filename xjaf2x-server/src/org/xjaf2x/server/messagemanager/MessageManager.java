package org.xjaf2x.server.messagemanager;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.Context;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.jboss.ejb3.annotation.Clustered;
import org.xjaf2x.server.JndiManager;
import org.xjaf2x.server.agentmanager.agent.AID;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;

/**
 * Default message manager implementation.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateless
@Remote(MessageManagerI.class)
@Clustered
public class MessageManager implements MessageManagerI
{
	private static final Logger logger;
	private static final ThreadPoolExecutor executor;
	private Cache<AID, AgentI> runningAgents;
	
	static
	{
		logger = Logger.getLogger(MessageManager.class.getName());
		executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		executor.setMaximumPoolSize(16);
	}
	
	@PostConstruct
	public void postConstruct()
	{
		try
		{	
			Context jndiContext = JndiManager.getContext();
			CacheContainer container = (CacheContainer) jndiContext.lookup("java:jboss/infinispan/container/xjaf2x-cache");
			runningAgents = container.getCache("running-agents");
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "MessageManager initialization error", ex);
		}
	}

	@Override
	public void post(final ACLMessage message)
	{
		final boolean info = logger.isLoggable(Level.INFO);
		for (AID aid : message.getReceivers())
		{
			final AgentI agent = runningAgents.get(aid);
			if (agent != null)
				executor.execute(new Runnable() {
					@Override
					public void run()
					{
						agent.onMessage(message);
					}
				});
			else if (info)
				logger.info("Agent not running: [" + aid + "]");
		}
		if (info)
			logger.info("Pool size: " + executor.getPoolSize());
	}
}
