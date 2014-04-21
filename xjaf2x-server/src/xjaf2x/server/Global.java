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

package xjaf2x.server;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import xjaf2x.server.agentmanager.AgentManager;
import xjaf2x.server.agentmanager.AgentManagerI;
import xjaf2x.server.agentmanager.agent.AID;
import xjaf2x.server.agentmanager.agent.AgentI;
import xjaf2x.server.messagemanager.MessageManager;
import xjaf2x.server.messagemanager.MessageManagerI;

/**
 * Global constants and utility functions.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class Global
{
	public static final String
		GROUP 		= "xjaf2x-group",
		USERNAME 	= "xjaf2xadmin",
		PASSWORD 	= "xjaf2xpass~",
		SERVER 		= "xjaf2x-server",
		MASTER_NAME = "xjaf2x-master";
	
	private static final Hashtable<String, Object> jndiProps = new Hashtable<>();
	private static final String AgentManagerLookup = "ejb:/" + Global.SERVER + "//"
			+ AgentManager.class.getSimpleName() + "!" + AgentManagerI.class.getName();
	private static final String MessageManagerLookup = "ejb:/" + Global.SERVER + "//"
			+ MessageManager.class.getSimpleName() + "!" + MessageManagerI.class.getName();

	static
	{
		jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
	}

	public static Context getContext() throws Exception
	{
		return new InitialContext(jndiProps);
	}

	public static AgentManagerI getAgentManager() throws Exception
	{
		return (AgentManagerI) getContext().lookup(AgentManagerLookup);
	}

	public static MessageManagerI getMessageManager() throws Exception
	{
		return (MessageManagerI) getContext().lookup(MessageManagerLookup);
	}

	public static Cache<AID, AgentI> getRunningAgents() throws Exception
	{
		CacheContainer container = (CacheContainer) getContext().lookup(
				"java:jboss/infinispan/container/xjaf2x-cache");
		Cache<AID, AgentI> cache = container.getCache("running-agents");
		if (cache == null)
			throw new Exception("Cannot load cache running-agents");
		return cache;
	}
}
