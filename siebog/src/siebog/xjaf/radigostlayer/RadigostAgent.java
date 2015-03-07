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

package siebog.xjaf.radigostlayer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import siebog.core.Global;
import siebog.interaction.ACLMessage;
import siebog.xjaf.agentmanager.AgentInitArgs;
import siebog.xjaf.core.Agent;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.core.XjafAgent;

/**
 * A placeholder for Radigost agents running on the server.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class RadigostAgent extends XjafAgent {
	private static final long serialVersionUID = 1L;
	public static final AgentClass AGENT_CLASS = new AgentClass(Global.SIEBOG_MODULE, RadigostAgent.class.getSimpleName());
	private String radigostSource;

	@PostConstruct
	public void postConstruct() {
		try {
			// TODO Replace absolute paths in JS loading.
			radigostSource = getJSSource("/home/dejan/dev/siebog/siebog/war/radigost.js");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	protected void onInit(AgentInitArgs args) {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		try {
			String js = getJSSource("/home/dejan/dev/siebog/siebog/war/radigost-agents/photo/PhotoAgent.js");
			final String src = radigostSource + "\nload(\"nashorn:mozilla_compat.js\");\n" + js;
			engine.eval(src);

			Invocable inv = (Invocable) engine;
			Object jsAgent = inv.invokeFunction("getAgentInstance");

			// inject state and signal arrival
			inv.invokeMethod(jsAgent, "setState", args.get("state"));
			inv.invokeMethod(jsAgent, "onArrived", Global.getNodeName(), true);
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ScriptException ex) {
			ex.printStackTrace();
		} catch (NoSuchMethodException ex) {
			ex.printStackTrace();
		}
	}

	private String getJSSource(String url) throws IOException {
		StringBuilder str = new StringBuilder();
		try (BufferedReader in = new BufferedReader(new FileReader(url))) {
			String line;
			while ((line = in.readLine()) != null) {
				if (!line.isEmpty() && !line.startsWith("importScripts"))
					str.append(line).append("\n");
			}
		}
		return str.toString();
	}

	@Override
	protected void onMessage(ACLMessage msg) {
	}
}
