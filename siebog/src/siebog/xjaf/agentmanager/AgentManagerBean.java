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

package siebog.xjaf.agentmanager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.infinispan.Cache;
import org.jboss.resteasy.annotations.Form;
import siebog.agents.xjaf.GUIAgent;
import siebog.agents.xjaf.RemoteAgent;
import siebog.jasonee.JasonEEAgent;
import siebog.utils.ContextFactory;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.Agent;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.radigostlayer.RadigostStub;

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
	private static final Logger logger = Logger.getLogger(AgentManagerBean.class.getName());
	private static Cache<AID, Agent> cache = ObjectFactory.getRunningAgentsCache();

	@PUT
	@Path("/running/{agClass}/{name}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Override
	public AID startAgent(@PathParam("agClass") AgentClass agClass, @PathParam("name") String name,
			@Form AgentInitArgs args) {

		AID aid;
		if (RadigostStub.AGENT_CLASS.equals(agClass)) {
			aid = new AID(name, args.get("host"), agClass);
		} else
			aid = new AID(name, agClass);

		if (cache.containsKey(aid)) {
			logger.info("Already running: " + aid);
			return aid;
		}

		try { // build the JNDI lookup string
			final String view = Agent.class.getName();
			String jndiNameStateless = String.format("ejb:/%s//%s!%s", agClass.getModule(), agClass.getEjbName(), view);
			String jndiNameStateful = jndiNameStateless + "?stateful";

			Agent agent = null;
			try {
				agent = ObjectFactory.lookup(jndiNameStateful, Agent.class);
			} catch (IllegalStateException ex) {
				// try the stateless version
				final Throwable cause = ex.getCause();
				if (cause == null || !(cause instanceof NamingException))
					throw ex;
				agent = ObjectFactory.lookup(jndiNameStateless, Agent.class);
			}

			// the order of the next two statements matters. if we call init first and the agent
			// sends a message from there, it sometimes happens that the reply arrives before we
			// register the AID. also some agents might wish to terminate themselves inside init.
			cache.put(aid, agent);
			agent.init(aid, args);
			logger.fine("Agent [" + aid + "] started.");
			return aid;
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Unable to start an agent of class " + agClass, ex);
			throw ex;
		}
	}

	@DELETE
	@Path("/running/{aid}")
	@Override
	public void stopAgent(@PathParam("aid") AID aid) {
		// TODO Implement 'agent stop'
	}

	@GET
	@Path("/classes")
	@Override
	public List<AgentClass> getAvailableAgentClasses() {
		Set<String> ignored = new HashSet<>();
		ignored.add(JasonEEAgent.class.getSimpleName());
		ignored.add(RemoteAgent.class.getSimpleName());
		ignored.add(GUIAgent.class.getSimpleName());

		final Context ctx = ContextFactory.get();
		List<AgentClass> result = new ArrayList<>();
		final String intf = "!" + Agent.class.getName();
		final String exp = "java:jboss/exported/";
		try {
			NamingEnumeration<NameClassPair> moduleList = ctx.list(exp);
			while (moduleList.hasMore()) {
				String module = moduleList.next().getName();
				NamingEnumeration<NameClassPair> agentList = ctx.list(exp + "/" + module);
				while (agentList.hasMore()) {
					String ejbName = agentList.next().getName();
					if (ejbName != null && ejbName.endsWith(intf)) {
						int n = ejbName.lastIndexOf(intf);
						ejbName = ejbName.substring(0, n);
						if (!ignored.contains(ejbName)) {
							AgentClass agClass = new AgentClass(module, ejbName);
							result.add(agClass);
						}
					}
				}
			}
		} catch (NamingException ex) {
			logger.log(Level.WARNING, "Error while loading deployed agents.", ex);
		}
		return result;
	}

	@GET
	@Path("/running")
	@Override
	public List<AID> getRunningAgents() {
		return new ArrayList<>(cache.keySet());
	}

	@Override
	public AID getAIDByRuntimeName(String runtimeName) {
		final List<AID> running = getRunningAgents();
		for (AID aid : running)
			if (aid.getName().equals(runtimeName))
				return aid;
		throw new IllegalArgumentException("No such agent: " + runtimeName);
	}

	@Override
	public void pingAgent(AID aid) {
		Agent agent = cache.get(aid);
		if (agent == null)
			throw new IllegalArgumentException("No such agent: " + aid);
		try {
			agent.ping();
		} catch (Exception ex) {
			throw new IllegalArgumentException("Unable to ping the agent.", ex);
		}
	}
}
