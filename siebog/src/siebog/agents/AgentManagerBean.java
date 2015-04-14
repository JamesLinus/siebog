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

package siebog.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.infinispan.Cache;
import org.jboss.resteasy.annotations.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import siebog.utils.GlobalCache;
import siebog.utils.ObjectFactory;

/**
 * Default agent manager implementation.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 * @author <a href="rade.milovanovic@hotmail.com">Rade Milovanovic</a>
 */
@Stateless
@Remote(AgentManager.class)
@LocalBean
@Path("/agents")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AgentManagerBean implements AgentManager {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(AgentManagerBean.class);
	private Cache<AID, Agent> agents;
	@Inject
	private JndiTreeParser jndiTreeParser;

	@Override
	public void startServerAgent(AID aid, AgentInitArgs args) {
		startServerAgent(aid, args, true);
	}

	@Override
	public void startServerAgent(AID aid, AgentInitArgs args, boolean replace) {
		if (getCache().containsKey(aid)) {
			if (!replace) {
				throw new IllegalStateException("Agent already running: " + aid);
			}
			stopAgent(aid);
		}
		Agent agent = null; 
		try {
			agent = ObjectFactory.lookup(getAgentLookup(aid.getAgClass(), true), Agent.class);
		} catch (IllegalStateException ex) {
			agent = ObjectFactory.lookup(getAgentLookup(aid.getAgClass(), false), Agent.class);
		}
		initAgent(agent, aid, args);
		LOG.info("Agent {} started.", aid.getStr());
	}

	public AID startServerAgent(AgentClass agClass, String runtimeName, AgentInitArgs args) {
		return startServerAgent(agClass, runtimeName, args, true);
	}

	@PUT
	@Path("/running/{agClass}/{name}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Override
	public AID startServerAgent(@PathParam("agClass") AgentClass agClass,
			@PathParam("name") String name, @Form AgentInitArgs args,
			@QueryParam("replace") @DefaultValue("true") boolean replace) {
		AID aid = new AID(name, agClass);
		startServerAgent(aid, args);
		return aid;
	}

	@Override
	public AID startClientAgent(AgentClass agClass, String name, AgentInitArgs args) {
		return null;
	}

	@DELETE
	@Path("/running/{aid}")
	@Override
	public void stopAgent(@PathParam("aid") AID aid) {
		Agent agent = getCache().get(aid);
		if (agent != null) {
			getCache().remove(aid);
			//agent.stop();
			LOG.info("Stopped agent: {}", aid);
		}
	}

	@GET
	@Path("/classes")
	@Override
	public List<AgentClass> getAvailableAgentClasses() {
		try {
			return jndiTreeParser.parse();
		} catch (NamingException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@GET
	@Path("/running")
	@Override
	public List<AID> getRunningAgents() {
		Set<AID> set = getCache().keySet();
		if (set.size() > 0) {
			try {
				AID aid = set.iterator().next();
				try {
					ObjectFactory.lookup(getAgentLookup(aid.getAgClass(), true), Agent.class);
				} catch (Exception ex) {
					ObjectFactory.lookup(getAgentLookup(aid.getAgClass(), false), Agent.class);
				}
			} catch (Exception ex) {
				getCache().clear();
				return new ArrayList<AID>();
			}
		}
		return new ArrayList<AID>(set);
	}

	@Override
	public AID getAIDByRuntimeName(String runtimeName) {
		AID aid = findInRunning(runtimeName, getRunningAgents());
		if (aid != null) {
			return aid;
		}
		throw new IllegalArgumentException("No such agent: " + runtimeName);
	}

	@Override
	public void pingAgent(AID aid) {
		try {
			Agent agent = getCache().get(aid);
			agent.ping();
		} catch (Exception ex) {
			throw new IllegalArgumentException("Unable to ping the agent.", ex);
		}
	}

	public Agent getAgentReference(AID aid) {
		// don't throw an exception here if there's no such agent
		return getCache().get(aid);
	}

	private Cache<AID, Agent> getCache() {
		if (agents == null)
			agents = GlobalCache.get().getRunningAgents();
		return agents;
	}

	private String getAgentLookup(AgentClass agClass, boolean stateful) {
		if (stateful)
			return String.format("ejb:/%s//%s!%s?stateful", agClass.getModule(), agClass.getEjbName(),
				Agent.class.getName());
		else
			return String.format("ejb:/%s//%s!%s", agClass.getModule(), agClass.getEjbName(),
					Agent.class.getName());
	}

	private void initAgent(Agent agent, AID aid, AgentInitArgs args) {
		// the order of the next two statements matters. if we call init first and the agent
		// sends a message from there, it sometimes happens that the reply arrives before we
		// register the AID. also some agents might wish to terminate themselves inside init.
		getCache().put(aid, agent);
		agent.init(aid, args);
	}

	private AID findInRunning(String runtimeName, List<AID> running) {
		for (AID aid : running) {
			if (aid.getName().equals(runtimeName)) {
				return aid;
			}
		}
		return null;
	}
}
