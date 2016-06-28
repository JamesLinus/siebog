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

import siebog.agentmanager.Agent;
import siebog.agentmanager.AgentManager;
import siebog.agentmanager.AgentManagerBean;
import siebog.clientmanager.WebClientManager;
import siebog.messagemanager.JMSFactory;
import siebog.messagemanager.MessageManager;
import siebog.messagemanager.MessageManagerBean;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class ObjectFactory {
	public static final String AgentManagerLookup = "ejb:/" + Agent.SIEBOG_MODULE + "//"
			+ AgentManagerBean.class.getSimpleName() + "!" + AgentManager.class.getName();
	public static final String MessageManagerLookup = "ejb:/" + Agent.SIEBOG_MODULE + "//"
			+ MessageManagerBean.class.getSimpleName() + "!" + MessageManager.class.getName();
	public static final String ExecutorServiceLookup = "java:global/" + Agent.SIEBOG_MODULE + "/"
			+ ExecutorService.class.getSimpleName() + "!" + ExecutorService.class.getName();
	public static final String WebClientManagerLookup = "ejb:/" + Agent.SIEBOG_MODULE + "//"
			+ WebClientManager.class.getSimpleName() + "!" + WebClientManager.class.getName()
			+ "?stateful";
	public static final String JMSFactoryLookup = "java:app/" + Agent.SIEBOG_MODULE + "/"
			+ JMSFactory.class.getSimpleName();

	public static AgentManager getAgentManager() {
		return lookup(AgentManagerLookup, AgentManager.class);
	}

	public static MessageManager getMessageManager() {
		return lookup(MessageManagerLookup, MessageManager.class);
	}

	public static WebClientManager getWebClientManager() {
		return lookup(WebClientManagerLookup, WebClientManager.class);
	}

	public static ExecutorService getExecutorService() {
		return lookup(ExecutorServiceLookup, ExecutorService.class);
	}

	public static SessionContext getSessionContext() {
		return lookup("java:comp/EJBContext", SessionContext.class);
	}

	/*public static JasonEEStarter getJasonEEStarter() {
		final String JasonEEStarterLookup = "ejb:/" + Agent.SIEBOG_MODULE + "//"
				+ JasonEEStarterImpl.class.getSimpleName() + "!" + JasonEEStarter.class.getName();
		return lookup(JasonEEStarterLookup, JasonEEStarter.class);
	}

	public static RemoteObjectFactory getRemoteObjectFactory(String module, String ejbName) {
		module = module.replace("\"", "");
		ejbName = ejbName.replace("\"", "");
		String name = "ejb:/" + module + "//" + ejbName + "!" + RemoteObjectFactory.class.getName();
		return lookup(name, RemoteObjectFactory.class);
	}*/

	public static JMSFactory getJMSFactory() {
		return lookup(JMSFactoryLookup, JMSFactory.class);
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
