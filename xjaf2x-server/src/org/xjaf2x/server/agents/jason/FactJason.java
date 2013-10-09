package org.xjaf2x.server.agents.jason;

import java.util.List;
import java.util.Map;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import jason.asSyntax.Literal;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.jboss.ejb3.annotation.Clustered;
import org.xjaf2x.server.JndiManager;
import org.xjaf2x.server.agentmanager.agent.AID;
import org.xjaf2x.server.agentmanager.agent.Agent;
import org.xjaf2x.server.agentmanager.agent.jason.JasonAgentI;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;

@Stateful(name = "org_xjaf2x_server_agents_jason_FactJason")
@Remote(JasonAgentI.class)
@Clustered
public class FactJason extends Agent implements JasonAgentI
{
	private static final long serialVersionUID = 1L;
	private int numAgents;
	private long startTime;

	@Override
	public void init(Map<String, Object> args) throws Exception
	{
		// get rid of trailing "s
		String str = (String) args.get("numAgents");
		str = str.substring(1, str.length() - 1);
		numAgents = Integer.parseInt(str);
	}

	@Override
	@Remove
	public void terminate()
	{
	}

	@Override
	public void onMessage(ACLMessage message)
	{
	}

	@Override
	public List<Literal> perceive()
	{
		return null;
	}

	@Override
	public boolean act(String functor)
	{
		if (functor.equals("start"))
			startTime = System.nanoTime();
		else
		{
			long total = (System.nanoTime() - startTime) / 1000000;
			try
			{
				CacheContainer container = (CacheContainer) JndiManager.getContext().lookup(
						"java:jboss/infinispan/container/xjaf2x-cache");
				Cache<AID, Long> cache = container.getCache("factJason");
				cache.put(aid, total);
				if (cache.size() == numAgents)
				{
					long sum = 0;
					for (Long tm : cache.values())
						sum += tm;
					System.out.println("Average time: " + (sum / numAgents) + " ms");
					cache.clear();
				}
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return true;
	}
}
