/**
 * Licensed to the Apache Software Foundation (ASF) under one 
 * or more contributor license agreements. See the NOTICE file 
 * distributed with this work for additional information regarding 
 * copyright ownership. The ASF licenses this file to you under 
 * the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may 
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. 
 * 
 * See the License for the specific language governing permissions 
 * and limitations under the License.
 */

package org.xjaf2x.server.agentmanager;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.jboss.ejb3.annotation.Clustered;
import org.xjaf2x.server.Global;
import org.xjaf2x.server.JndiManager;
import org.xjaf2x.server.agentmanager.agent.AID;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.agentmanager.agent.jason.JasonAgentI;
import org.xjaf2x.server.config.AgentDesc;

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
	private Cache<String, AgentDesc> deployedAgents;
	
	@PostConstruct
	public void postConstruct()
	{
		try
		{	
			jndiContext = JndiManager.getContext();
			CacheContainer container = (CacheContainer) jndiContext.lookup("java:jboss/infinispan/container/xjaf2x-cache");
			runningAgents = container.getCache("running-agents");
			deployedAgents = container.getCache("deployed-agents");
			if (deployedAgents.size() == 0)
				reloadDeployedAgents();
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "AgentManager initialization error", ex);
		}
	}
	
	@Override
	public AID startAgent(String family, String runtimeName, Serializable[] args)
	{
		AID aid = new AID(runtimeName, family);
		// is it running already?
		AgentI agent = runningAgents.get(aid);
		if (agent != null)
		{
			if (logger.isLoggable(Level.FINE))
				logger.info("Already running: [" + aid + "]");
			return aid;
		}
		
		AgentDesc rec = deployedAgents.get(family);
		if (rec == null)
		{
			if (logger.isLoggable(Level.INFO))
				logger.info("No such family name: [" + family + "]");
			return null;
		}
		
		agent = createNew(rec.getJndiName(), aid, args);
		if (agent == null)
			return null;
		if (logger.isLoggable(Level.FINE))
			logger.fine("Agent [" + aid + "] running.");
		return aid;
	}
	
	@Override
	public JasonAgentI startJasonAgent(String family, String runtimeName, Serializable[] args)
	{
		AID aid = startAgent(family, runtimeName, args);
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

	private AgentI createNew(String jndiName, AID aid, Serializable[] args)
	{
		try
		{
			AgentI agent = (AgentI) jndiContext.lookup(jndiName);
			agent.setAid(aid);
			agent.init(args);
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
			final String INTF = "!" + AgentI.class.getName();
			NamingEnumeration<NameClassPair> list = jndiContext.list("java:jboss/exported/xjaf2x-server");
			while (list.hasMore())
			{
				NameClassPair ncp = list.next();
				final String name = ncp.getName();
				if (name.endsWith(INTF))
				{
					String family = name.substring(0, name.indexOf('!'));
					try
					{
						Class<?> cls = Class.forName(family.replace('_', '.'));
						// stateful or stateless?
						boolean stateful = cls.getAnnotation(Stateful.class) != null;
						// jason?
						boolean jason = false; // TODO : include search for Jason agents
						// ok, store
						AgentDesc desc = new AgentDesc(family, stateful, Global.SERVER, jason);
						deployedAgents.put(family, desc);
						
					} catch (ClassNotFoundException ex)
					{
						logger.log(Level.WARNING, "Error while performing a class lookup for [" + family + "]", ex);
						continue;
					}
				}
			}
			if (logger.isLoggable(Level.INFO))
				logger.info("Successfully reloaded [" + deployedAgents.size() + "] agents");
		} catch (NamingException ex)
		{
			logger.log(Level.WARNING, "Error while reloading deployed agents", ex);
		}
	}

	@Override
	public Set<AID> getRunning()
	{
		Set<AID> aids = new HashSet<>(runningAgents.keySet().size());
		aids.addAll(runningAgents.keySet());
		return aids;
	}
}
