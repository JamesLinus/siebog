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

package siebog.utils;

import javax.ejb.SessionContext;
import javax.naming.NamingException;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import siebog.core.Global;
import siebog.jasonee.JasonEEStarter;
import siebog.jasonee.JasonEEStarterImpl;
import siebog.jasonee.RemoteObjectFactory;
import siebog.jasonee.control.ExecutionControl;
import siebog.radigost.websocket.bridges.BridgeManager;
import siebog.xjaf.core.AID;
import siebog.xjaf.managers.AgentManager;
import siebog.xjaf.managers.AgentManagerImpl;
import siebog.xjaf.managers.MessageManager;
import siebog.xjaf.managers.MessageManagerImpl;
import siebog.xjaf.managers.RunningAgent;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class ObjectFactory {
	private static final String AgentManagerLookup = "ejb:/" + Global.SERVER + "//"
			+ AgentManagerImpl.class.getSimpleName() + "!" + AgentManager.class.getName();
	private static final String MessageManagerLookup = "ejb:/" + Global.SERVER + "//"
			+ MessageManagerImpl.class.getSimpleName() + "!" + MessageManager.class.getName();
	private static final String BridgeManagerLookup = "ejb:/" + Global.SERVER + "//"
			+ BridgeManager.class.getSimpleName();
	private static final String ExecutorServiceLookup = "java:global/" + Global.SERVER + "/"
			+ ExecutorService.class.getSimpleName() + "!" + ExecutorService.class.getName();
	private static final String XjafCacheLookup = "java:jboss/infinispan/container/xjaf2x-cache";
	private static final String JasonEEStarterLookup = "ejb:/" + Global.SERVER + "//"
			+ JasonEEStarterImpl.class.getSimpleName() + "!" + JasonEEStarter.class.getName();

	public static AgentManager getAgentManager() {
		return lookup(AgentManagerLookup, AgentManager.class);
	}

	public static MessageManager getMessageManager() {
		return lookup(MessageManagerLookup, MessageManager.class);
	}

	public static BridgeManager getBridgeManager() {
		return lookup(BridgeManagerLookup, BridgeManager.class);
	}

	public static ExecutorService getExecutorService() {
		return lookup(ExecutorServiceLookup, ExecutorService.class);
	}

	public static SessionContext getSessionContext() {
		return lookup("java:comp/EJBContext", SessionContext.class);
	}

	private static CacheContainer getCacheContainer() {
		return lookup(XjafCacheLookup, CacheContainer.class);
	}

	public static Cache<AID, RunningAgent> getRunningAgentsCache() {
		Cache<AID, RunningAgent> cache = getCacheContainer().getCache("running-agents");
		if (cache == null)
			throw new IllegalStateException("Cannot load cache running-agents.");
		return cache;
	}

	public static Cache<String, ExecutionControl> getExecutionControlCache() {
		Cache<String, ExecutionControl> cache = getCacheContainer().getCache("execution-controls");
		if (cache == null)
			throw new IllegalStateException("Cannot load cache execution-controls.");
		return cache;
	}

	public static JasonEEStarter getJasonEEStarter() {
		return lookup(JasonEEStarterLookup, JasonEEStarter.class);
	}

	public static RemoteObjectFactory getRemoteObjectFactory(String module, String ejbName) {
		module = module.replace("\"", "");
		ejbName = ejbName.replace("\"", "");
		String name = "ejb:/" + module + "//" + ejbName + "!" + RemoteObjectFactory.class.getName();
		return lookup(name, RemoteObjectFactory.class);
	}

	@SuppressWarnings("unchecked")
	public static <T> T lookup(String name, Class<T> c) {
		try {
			return (T) ContextFactory.get().lookup(name);
		} catch (NamingException ex) {
			throw new IllegalStateException("Failed to lookup " + name, ex);
		}
	}
}
