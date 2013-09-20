package org.xjaf2x.server.agentmanager;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.jboss.ejb3.annotation.Clustered;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xjaf2x.server.Global;
import org.xjaf2x.server.JndiManager;
import org.xjaf2x.server.agentmanager.agent.AID;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.agentmanager.agent.jason.JasonAgentI;

/**
 * Default agent manager implementation.
 * 
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateless
@Remote(AgentManagerI.class)
@Clustered
public class AgentManager implements AgentManagerI
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AgentManager.class.getName());
	private Context jndiContext;
	private Cache<AID, AgentI> runningAgents;
	private Cache<String, AgentRec> deployedAgents;
	
	@PostConstruct
	public void postConstruct()
	{
		try
		{	
			jndiContext = JndiManager.getContext();
			CacheContainer container = (CacheContainer) jndiContext.lookup("java:jboss/infinispan/container/xjaf2x-cache");
			runningAgents = container.getCache("running-agents");
			deployedAgents = container.getCache("deployed-agents");
			// TODO : reload should be done only the first time; can the container return 'null' if the map is not found
			//if (deployedAgents.size() == 0)
				reloadDeployedAgents();
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "AgentManager initialization error", ex);
		}
	}
	
	@Override
	public AID startAgent(String family, String runtimeName)
	{
		AID aid = new AID(runtimeName, family);
		AgentI agent = runningAgents.get(aid);
		if (agent != null)
		{
			if (logger.isLoggable(Level.FINE))
				logger.info("Already running: [" + aid + "]");
			return aid;
		}
		
		AgentRec rec = deployedAgents.get(family);
		if (rec == null)
		{
			if (logger.isLoggable(Level.INFO))
				logger.info("No such family name: [" + family + "]");
			return null;
		}
		
		agent = createNew(rec.getJndiName(), aid);
		if (agent == null)
			return null;
		if (logger.isLoggable(Level.FINE))
			logger.fine("Agent [" + aid + "] running.");
		return aid;
	}
	
	@Override
	public JasonAgentI startJasonAgent(String family, String runtimeName)
	{
		AID aid = startAgent(family, runtimeName);
		if (aid == null)
			return null;
		return (JasonAgentI) runningAgents.get(aid);
	}
	
	/**
	 * Terminates an active agent.
	 * 
	 * @param aid AID object.
	 */
	@Override
	public void stopAgent(AID aid)
	{
		AgentI agent = runningAgents.get(aid);
		if (agent != null)
		{
			runningAgents.remove(aid);
			agent.terminate();
		}
	}

	private AgentI createNew(String jndiName, AID aid)
	{
		try
		{
			AgentI agent = (AgentI) jndiContext.lookup(jndiName);
			agent.setAid(aid);
			runningAgents.put(aid, agent);
			return agent;
		} catch (Exception ex)
		{
			if (logger.isLoggable(Level.INFO))
				logger.log(Level.INFO, "Error while performing a lookup of [" + jndiName + "]", ex);
			deployedAgents.remove(aid.getFamily());
			return null;
		}
	}
	
	@Override
	public Set<String> getFamilies()
	{
		Set<String> result = new HashSet<>();
		result.addAll(deployedAgents.keySet());
		return result;
	}
	
	private void reloadDeployedAgents()
	{
		try
		{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			try (InputStream is = getClass().getResourceAsStream("/org/xjaf2x/server/agents/xjaf2x-agents.xml"))
			{
				Document doc = builder.parse(is);
				NodeList agentNodeList = doc.getElementsByTagName("agent");
				for (int i = 0; i < agentNodeList.getLength(); i++)
				{
					Node agentNode = agentNodeList.item(i);
					NamedNodeMap attrib = agentNode.getAttributes();
					// family
					String family = getNodeValue(attrib, "family");
					// jason?
					boolean jason = "true".equalsIgnoreCase(getNodeValue(attrib, "jason"));
					// stateful or stateless?
					boolean stateless;
					if (jason)
						stateless = false;
					else
						stateless = "false".equalsIgnoreCase(getNodeValue(attrib, "stateful"));
					
					// ok?
					if (family != null)
					{
						AgentRec rec = new AgentRec(family, !stateless, Global.SERVER, jason);
						deployedAgents.put(family, rec);
					}
				}
			}
			if (logger.isLoggable(Level.INFO))
				logger.info("Successfully reloaded [" + deployedAgents.size() + "] agents");
		} catch (Exception ex)
		{
			logger.log(Level.WARNING, "Error while reloading deployed agents", ex);
		}
	}
	
	private String getNodeValue(NamedNodeMap map, String name)
	{
		Node node = map.getNamedItem(name);
		return node != null ? node.getNodeValue() : null;
	}

	@Override
	public Set<AID> getRunning()
	{
		Set<AID> aids = new HashSet<>(runningAgents.keySet().size());
		aids.addAll(runningAgents.keySet());
		return aids;
	}
}
