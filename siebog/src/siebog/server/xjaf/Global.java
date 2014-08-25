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
import javax.naming.NamingException;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import siebog.server.utils.ContextFactory;
import siebog.server.xjaf.core.AID;
import siebog.server.xjaf.core.Agent;

/**
 * Global constants and utility functions.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class Global {
	public static final String VERSION = "1.0.1";

	//@formatter:off
	public static final String 
		GROUP = "xjaf2x-group", 
		USERNAME = "xjaf2xadmin", 
		PASSWORD = "xjaf2xpass~",
		SERVER = "siebog", 
		MASTER_NAME = "xjaf-master";
	//@formatter:on

	private static final JSONParser jsonParser = new JSONParser();

	public static Cache<AID, Agent> getRunningAgents() {
		try {
			final String name = "java:jboss/infinispan/container/xjaf2x-cache";
			CacheContainer container = (CacheContainer) ContextFactory.lookup(name);
			Cache<AID, Agent> cache = container.getCache("running-agents");
			if (cache == null)
				throw new IllegalStateException("Cannot load cache running-agents.");
			return cache;
		} catch (NamingException ex) {
			throw new IllegalStateException("Cannot lookup xjaf2x-cache.");
		}
	}

	public static String readFile(String fileName) throws IOException {
		return readFile(new FileInputStream(fileName));
	}

	public static String readFile(InputStream in) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			StringBuilder str = new StringBuilder(in.available());
			String line;
			String nl = "";
			while ((line = reader.readLine()) != null) {
				str.append(nl);
				if (nl.length() == 0)
					nl = "\n";
				str.append(line);
			}
			return str.toString();
		}
	}

	public static void writeFile(File file, String data) throws IOException {
		try (PrintWriter out = new PrintWriter(file)) {
			out.print(data);
		}
	}

	public static void printVersion() {
		System.out.println("-------------------------------------------------------------");
		System.out.println("Siebog Multiagent Framework v" + VERSION);
		System.out.println("-------------------------------------------------------------");
	}

	public static JSONObject parseJson(String json) throws ParseException {
		return (JSONObject) jsonParser.parse(json);
	}

	public static String getNodeName() {
		return System.getProperty("jboss.node.name");
	}
}
