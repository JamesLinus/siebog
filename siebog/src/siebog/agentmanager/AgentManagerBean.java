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

package siebog.agentmanager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.NamingException;

import org.infinispan.Cache;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import siebog.connectionmanager.ConnectionManagerRestAPI;
import siebog.utils.GlobalCache;
import siebog.utils.LoggerUtil;
import siebog.utils.LoggerUtil.SocketMessageType;
import siebog.utils.ObjectFactory;
import siebog.utils.ObjectField;

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
public class AgentManagerBean implements AgentManager {
	private static final long serialVersionUID = 1L;
	private Cache<AID, Agent> agents;
	@Inject
	private JndiTreeParser jndiTreeParser;

	@Override
	public void startServerAgent(AID aid, AgentInitArgs args, boolean replace) {
		if (getCache().containsKey(aid)) {
			if (!replace) {
				throw new IllegalStateException("Agent already running: " + aid);
			}
			stopAgent(aid);
			if(args == null || args.get("noUIUpdate", "").equals("")) {
				LoggerUtil.logAgent(aid, SocketMessageType.REMOVE);
			}
		}
		Agent agent = null;
		try {
			agent = ObjectFactory.lookup(getAgentLookup(aid.getAgClass(), true), Agent.class);
		} catch (IllegalStateException ex) {
			agent = ObjectFactory.lookup(getAgentLookup(aid.getAgClass(), false), Agent.class);
		}
		initAgent(agent, aid, args);
		LoggerUtil.log("Agent " + aid.getStr() + " started. AID: " + aid.toString(), true);
		if(args == null || args.get("noUIUpdate", "").equals("")) {
			LoggerUtil.logAgent(aid, SocketMessageType.ADD);
		}
	}

	@Override
	public AID startServerAgent(AgentClass agClass, String runtimeName, AgentInitArgs args) {
		String host = AID.HOST_NAME;
		if (args != null) {
			host = args.get("host", AID.HOST_NAME);
		}
		AID aid = new AID(runtimeName, host, agClass);
		startServerAgent(aid, args, true);
		return aid;
	}

	@Override
	public AID startClientAgent(AgentClass agClass, String name, AgentInitArgs args) {
		return null;
	}

	@Override
	public void stopAgent(AID aid) {
		Agent agent = getCache().get(aid);
		if (agent != null) {
			getCache().remove(aid);

			LoggerUtil.log("Stopped agent: " + aid, true);
			LoggerUtil.logAgent(aid, SocketMessageType.REMOVE);
		}
	}

	@Override
	public List<AgentClass> getAvailableAgentClasses() {
		try {
			return jndiTreeParser.parse();
		} catch (NamingException ex) {
			throw new IllegalStateException(ex);
		}
	}

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
		// don't throw an exception if not found, because it will be intercepted
		return findInRunning(runtimeName, getRunningAgents());
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
			return String.format("ejb:/%s//%s!%s?stateful", agClass.getModule(),
					agClass.getEjbName(), Agent.class.getName());
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
	
	@Override
	public void move(AID aid, String host) {
		clone(aid, host);
		stopAgent(aid);
	}
	
	@Override
	public void clone(AID aid, String host) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://"+host+"/siebog/rest/connection");
		ConnectionManagerRestAPI rest = rtarget.proxy(ConnectionManagerRestAPI.class);
		Agent a = getAgentReference(aid);
		rest.moveAgent(a.deconstruct());
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void reconstructAgent(List<ObjectField> agent) {
		Agent localAgent = null;
		for(int i = 0; i < agent.size(); i++) {
			if(agent.get(i).getName().equals("Aid")) {
				LinkedHashMap<String, Object> aidMap = (LinkedHashMap) agent.get(i).getValue();
				LinkedHashMap<String, Object> agClassMap = (LinkedHashMap) aidMap.get("agClass");
				
				AID aid = new AID((String)aidMap.get("name"), (String)aidMap.get("host"), new AgentClass((String)agClassMap.get("module"), (String)agClassMap.get("ejbName"), (String)agClassMap.get("path")));
				localAgent = getAgentReference(startServerAgent(aid.getAgClass(), aid.getName(), null));
				agent.remove(i);
				break;
			}
		}
		localAgent.reconstruct(agent);
	}
}
