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

package xjaf2x.server.agentmanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.jboss.ejb3.annotation.Clustered;
import xjaf2x.server.Global;
import xjaf2x.server.agentmanager.agent.AID;
import xjaf2x.server.agentmanager.agent.AgentI;
import xjaf2x.server.agentmanager.agent.jason.JasonAgentI;

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

	@PostConstruct
	public void postConstruct()
	{
		try
		{
			jndiContext = Global.getContext();
			runningAgents = Global.getRunningAgents();
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "AgentManager initialization error", ex);
		}
	}

	@Override
	public AID start(String family, String runtimeName, Serializable... args)
	{
		AID aid = new AID(family, runtimeName);
		// is it running already?
		AgentI agent = runningAgents.get(aid);
		if (agent != null)
		{
			logger.fine("Already running: [" + aid + "]");
			return aid;
		}

		agent = createNew(family, aid, args);
		if (agent == null)
			return null;
		logger.fine("Agent [" + aid + "] running.");
		return aid;
	}

	@Override
	public JasonAgentI startJasonAgent(String family, String runtimeName, Serializable[] args)
	{
		AID aid = start(family, runtimeName, args);
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
	public void stop(AID aid)
	{
		AgentI agent = runningAgents.get(aid);
		if (agent != null)
		{
			runningAgents.remove(aid);
			agent.terminate();
		}
	}

	private AgentI createNew(String family, AID aid, Serializable... args)
	{
		try
		{
			Class<?> cls = Class.forName(family.replace('_', '.'));
			// build the JNDI lookup string
			final String view = AgentI.class.getName();
			String jndiName = String.format("ejb:/%s//%s!%s", Global.SERVER, family, view);
			if (cls.getAnnotation(Stateful.class) != null) // stateful EJB
				jndiName += "?stateful";
			AgentI agent = (AgentI) jndiContext.lookup(jndiName);
			// the order of the next two statements matters. if we call init first and the agent
			// sends a message from there, it sometimes happens that the reply arrives before we
			// register the AID. also some agents might wish to terminate themselves inside init.
			runningAgents.put(aid, agent);
			agent.init(aid, args);
			return agent;
		} catch (Exception ex)
		{
			if (logger.isLoggable(Level.INFO))
				logger.log(Level.INFO, "Error while creating [" + aid + "]", ex);
			return null;
		}
	}

	@Override
	public List<String> getFamilies()
	{
		List<String> result = new ArrayList<>();
		final String intf = "!" + AgentI.class.getName();
		try
		{
			NamingEnumeration<NameClassPair> list = jndiContext.list("java:jboss/exported/"
					+ Global.SERVER);
			while (list.hasMore())
			{
				NameClassPair ncp = list.next();
				final String name = ncp.getName();
				if (name.endsWith(intf))
				{
					String family = name.substring(0, name.lastIndexOf('!'));
					result.add(family);

				}
			}
		} catch (NamingException ex)
		{
			logger.log(Level.WARNING, "Error while loading agent families", ex);
		}
		return result;
	}

	@Override
	public List<AID> getRunning()
	{
		final Set<AID> keys = runningAgents.keySet();
		List<AID> aids = new ArrayList<>(keys.size());
		aids.addAll(keys);
		return aids;
	}

	@Override
	public List<AID> getRunning(AID pattern)
	{
		List<AID> aids = new ArrayList<>();
		Iterator<AID> i = runningAgents.keySet().iterator();
		while (i.hasNext())
		{
			AID aid = i.next();
			if (aid.matches(pattern))
				aids.add(aid);
		}
		return aids;
	}
}
