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

package siebog.server.xjaf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import siebog.server.xjaf.core.AID;
import siebog.server.xjaf.core.Agent;
import siebog.server.xjaf.managers.AgentManagerImpl;
import siebog.server.xjaf.managers.AgentManager;
import siebog.server.xjaf.managers.MessageManagerImpl;
import siebog.server.xjaf.managers.MessageManager;

/**
 * Global constants and utility functions.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class Global
{
	public static final String VERSION = "1.0.1";
	
	public static final String 
		GROUP = "xjaf2x-group", 
		USERNAME = "xjaf2xadmin",
		PASSWORD = "xjaf2xpass~", 
		SERVER = "siebog", 
		MASTER_NAME = "xjaf-master";

	private static final String AgentManagerLookup = "ejb:/" + SERVER + "//"
			+ AgentManagerImpl.class.getSimpleName() + "!" + AgentManager.class.getName();
	private static final String MessageManagerLookup = "ejb:/" + SERVER + "//"
			+ MessageManagerImpl.class.getSimpleName() + "!" + MessageManager.class.getName();
	private static final Logger logger = Logger.getLogger(Global.class.getName());
	private static Context context;
	private static final JSONParser jsonParser = new JSONParser();

	static
	{
		try
		{
			Hashtable<String, Object> jndiProps = new Hashtable<>();
			jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			context = new InitialContext(jndiProps);
		} catch (NamingException ex)
		{
			logger.log(Level.SEVERE, "Context initialization error.", ex);
		}
	}

	public static Context getContext()
	{
		return context;
	}

	public static AgentManager getAgentManager() throws IllegalStateException
	{
		try
		{
			return (AgentManager) getContext().lookup(AgentManagerLookup);
		} catch (NamingException ex)
		{
			throw new IllegalStateException("Failed to lookup agent manager.", ex);
		}
	}

	public static MessageManager getMessageManager() throws IllegalStateException
	{
		try
		{
			return (MessageManager) getContext().lookup(MessageManagerLookup);
		} catch (NamingException ex)
		{
			throw new IllegalStateException("Failed to lookup message manager.", ex);
		}
	}

	public static Cache<AID, Agent> getRunningAgents() throws NamingException
	{
		CacheContainer container = (CacheContainer) getContext().lookup(
				"java:jboss/infinispan/container/xjaf2x-cache");
		Cache<AID, Agent> cache = container.getCache("running-agents");
		if (cache == null)
			throw new IllegalStateException("Cannot load cache running-agents.");
		return cache;
	}
	
	public static String readFile(String fileName) throws IOException
	{
		return readFile(new FileInputStream(fileName));
	}
	
	public static String readFile(InputStream in) throws IOException
	{
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in)))
		{
			StringBuilder str = new StringBuilder(in.available());
			String line;
			String nl = "";
			while ((line = reader.readLine()) != null)
			{
				str.append(nl);
				if (nl.length() == 0)
					nl = "\n";
				str.append(line);
			}
			return str.toString();
		} 
	}
	
	public static void writeFile(File file, String data) throws IOException
	{
		try (PrintWriter out = new PrintWriter(file))
		{
			out.print(data);
		}
	}
	
	public static void printVersion()
	{
		System.out.println("-------------------------------------------------------------");
		System.out.println("Siebog Multiagent Framework v" + VERSION);
		System.out.println("-------------------------------------------------------------");
	}
	
	public static JSONObject parseJson(String json) throws ParseException
	{
		return (JSONObject) jsonParser.parse(json);
	}
	
	public static String getNodeName()
	{
		return System.getProperty("jboss.node.name");
	}
}
