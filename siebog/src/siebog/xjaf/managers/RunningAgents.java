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

package siebog.xjaf.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.NamingException;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import siebog.utils.ContextFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.Agent;
import siebog.xjaf.core.AgentClass;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Singleton
@LocalBean
@Startup
public class RunningAgents {
	private static class RunningAgentRec {
		Agent ref;
	}

	private Cache<AID, RunningAgentRec> cache;

	@PostConstruct
	public void postConstruct() {
		try {
			final String name = "java:jboss/infinispan/container/xjaf2x-cache";
			CacheContainer container = (CacheContainer) ContextFactory.lookup(name);
			cache = container.getCache("running-agents");
			if (cache == null)
				throw new IllegalStateException("Cannot load cache running-agents.");
		} catch (NamingException ex) {
			throw new IllegalStateException("Cannot lookup xjaf2x-cache.", ex);
		}
	}

	public boolean isRunning(AID aid) {
		return cache.containsKey(aid);
	}

	public void start(AgentClass agClass, AID aid, AgentInitArgs args) throws NamingException {
		// build the JNDI lookup string
		final String view = Agent.class.getName();
		String jndiNameStateless = String.format("ejb:/%s//%s!%s", agClass.getModule(), agClass.getEjbName(), view);
		String jndiNameStateful = jndiNameStateless + "?stateful";

		Agent agent = null;
		try {
			agent = (Agent) ContextFactory.lookup(jndiNameStateful);
		} catch (NamingException ex) {
			final Throwable cause = ex.getCause();
			if (cause == null || !(cause instanceof IllegalStateException))
				throw ex;
			agent = (Agent) ContextFactory.lookup(jndiNameStateless);
		}

		RunningAgentRec rec = new RunningAgentRec();
		rec.ref = agent;
		// the order of the next two statements matters. if we call init first and the agent
		// sends a message from there, it sometimes happens that the reply arrives before we
		// register the AID. also some agents might wish to terminate themselves inside init.
		cache.put(aid, rec);
		final Map<String, String> argsMap = args != null ? args.toStringMap() : null;
		agent.init(aid, argsMap);
	}

	public void stop(AID aid) {
		// TODO Implement 'agent stop'
	}

	public Agent getAgentReference(AID aid) {
		RunningAgentRec rec = cache.get(aid);
		if (rec == null)
			throw new IllegalArgumentException("No such AID: " + aid);
		return rec.ref;
	}

	public List<AID> getAIDs() {
		return new ArrayList<>(cache.keySet());
	}

	public AID getAIDByRuntimeName(String runtimeName) {
		final List<AID> aids = getAIDs();
		for (AID aid : aids)
			if (aid.getName().equals(runtimeName))
				return aid;
		throw new IllegalArgumentException("No such agent: " + runtimeName);
	}
}
