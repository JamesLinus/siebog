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

package siebog.radigost.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import siebog.agents.AgentClass;
import siebog.agents.AgentInitArgs;
import siebog.agents.XjafAgent;
import siebog.core.Global;

/**
 * A placeholder for Radigost agents that have migrated to the server.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class AgentPlaceholder extends XjafAgent {
	private static final long serialVersionUID = 1L;
	public static final AgentClass AGENT_CLASS = AgentClass.forSiebogEjb(AgentPlaceholder.class);
	private String radigostSource;

	@Override
	protected void onInit(AgentInitArgs args) {
		radigostSource = getJSSource("/home/dejan/dev/siebog/siebog/war/radigost.js");
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		try {
			String jspath = args.get("jspath");
			engine.eval(getFullAgentSouce(jspath));
			Invocable inv = (Invocable) engine;
			Object jsAgent = inv.invokeFunction("getAgentInstance");
			// inject state and signal arrival
			inv.invokeMethod(jsAgent, "setState", args.get("state"));
			inv.invokeMethod(jsAgent, "onArrived", Global.getNodeName(), true);
		} catch (ScriptException | NoSuchMethodException ex) {
			throw new IllegalStateException(ex);
		}
	}

	protected String getJSSource(String url) {
		try (BufferedReader in = new BufferedReader(new FileReader(url))) {
			return readJSSource(in);
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private String readJSSource(BufferedReader in) throws IOException {
		StringBuilder str = new StringBuilder();
		String line;
		while ((line = in.readLine()) != null) {
			if (!line.isEmpty() && !line.startsWith("importScripts"))
				str.append(line).append("\n");
		}
		return str.toString();
	}

	private String getFullAgentSouce(String jspath) {
		String js = getJSSource(jspath);
		StringBuilder sb = new StringBuilder(radigostSource);
		sb.append("\nload(\"nashorn:mozilla_compat.js\");\n");
		sb.append(js);
		return sb.toString();
	}
}
