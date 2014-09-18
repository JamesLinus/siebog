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
import siebog.jasonee.JasonEEAppImpl;
import siebog.jasonee.RemoteObjectFactory;
import siebog.jasonee.control.ExecutionControl;
import siebog.jasonee.control.ExecutionControlImpl;
import siebog.jasonee.environment.Environment;
import siebog.jasonee.environment.EnvironmentImpl;
import siebog.jasonee.intf.JasonEEApp;
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
	private static final String JasonEEEnvLookup = "ejb:/" + Global.SERVER + "//"
			+ EnvironmentImpl.class.getSimpleName() + "!" + Environment.class.getName() + "?stateful";
	public static final String JasonEEExecCtrlLookup = "ejb:/" + Global.SERVER + "//"
			+ ExecutionControlImpl.class.getSimpleName() + "!" + ExecutionControl.class.getName() + "?stateful";
	private static final String JasonEEAppLookup = "ejb:/" + Global.SERVER + "//"
			+ JasonEEAppImpl.class.getSimpleName() + "!" + JasonEEApp.class.getName();

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

	public static Cache<AID, RunningAgent> getRunningAgentsCache() {
		CacheContainer container = lookup(XjafCacheLookup, CacheContainer.class);
		Cache<AID, RunningAgent> cache = container.getCache("running-agents");
		if (cache == null)
			throw new IllegalStateException("Cannot load cache running-agents.");
		return cache;
	}

	public static Cache<String, Environment> getEnvironmentCache() {
		CacheContainer container = lookup(XjafCacheLookup, CacheContainer.class);
		Cache<String, Environment> cache = container.getCache("jasonee-envs");
		if (cache == null)
			throw new IllegalStateException("Cannot load cache jasonee-envs.");
		return cache;
	}

	public static Cache<String, ExecutionControl> getExecutionControlCache() {
		CacheContainer container = lookup(XjafCacheLookup, CacheContainer.class);
		Cache<String, ExecutionControl> cache = container.getCache("jasonee-ctrls");
		if (cache == null)
			throw new IllegalStateException("Cannot load cache jasonee-ctrls.");
		return cache;
	}

	public static Environment getJasonEEEnvironment() {
		return lookup(JasonEEEnvLookup, Environment.class);
	}

	public static ExecutionControl getExecutionControl() {
		return lookup(JasonEEExecCtrlLookup, ExecutionControl.class);
	}

	public static JasonEEApp getJasonEEApp() {
		return lookup(JasonEEAppLookup, JasonEEApp.class);
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
