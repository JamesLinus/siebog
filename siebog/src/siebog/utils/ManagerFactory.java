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

import javax.naming.NamingException;
import siebog.core.Global;
import siebog.radigost.websocket.bridges.BridgeManager;
import siebog.xjaf.managers.AgentManager;
import siebog.xjaf.managers.AgentManagerImpl;
import siebog.xjaf.managers.MessageManager;
import siebog.xjaf.managers.MessageManagerImpl;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class ManagerFactory {
	private static final String AgentManagerLookup = "ejb:/" + Global.SERVER + "//"
			+ AgentManagerImpl.class.getSimpleName() + "!" + AgentManager.class.getName();
	private static final String MessageManagerLookup = "ejb:/" + Global.SERVER + "//"
			+ MessageManagerImpl.class.getSimpleName() + "!" + MessageManager.class.getName();
	private static final String BridgeManagerLookup = "ejb:/" + Global.SERVER + "//"
			+ BridgeManager.class.getSimpleName();

	public static AgentManager getAgentManager() {
		try {
			return (AgentManager) ContextFactory.lookup(AgentManagerLookup);
		} catch (NamingException ex) {
			throw new IllegalStateException("Failed to lookup agent manager.", ex);
		}
	}

	public static MessageManager getMessageManager() {
		try {
			return (MessageManager) ContextFactory.lookup(MessageManagerLookup);
		} catch (NamingException ex) {
			throw new IllegalStateException("Failed to lookup message manager.", ex);
		}
	}

	public static BridgeManager getBridgeManager() {
		try {
			return (BridgeManager) ContextFactory.lookup(BridgeManagerLookup);
		} catch (NamingException ex) {
			throw new IllegalStateException("Failed to lookup bridge manager.", ex);
		}
	}
}
