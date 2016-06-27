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

package siebog.radigost.stub;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class ScriptLoader {
	private String radigostSource;

	public ScriptLoader() {
		radigostSource = getJSSource("siebog/agents/test/js/agent.js");
	}

	public Invocable load(String url, String state) throws ScriptException, NoSuchMethodException {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		engine.eval(getFullAgentSouce(url));
		Invocable invocable = (Invocable) engine;
		Object jsAgent = invocable.invokeFunction("getAgentInstance");
		// inject state and signal arrival
		invocable.invokeMethod(jsAgent, "setState", state);
		invocable.invokeMethod(jsAgent, "onArrived", System.getProperty("jboss.node.name"), true);
		return invocable;
	}

	protected String getJSSource(String name) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(name);
		try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
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

	private String getFullAgentSouce(String url) {
		String jsFileName = url.split("/")[url.split("/").length - 1];
		String clientToServerUrl = "siebog" + File.separator + "agents" + File.separator + "test" + File.separator + "js" + File.separator + jsFileName;
		String js = getJSSource(clientToServerUrl);
		StringBuilder sb = new StringBuilder(radigostSource);
		sb.append("\nload(\"nashorn:mozilla_compat.js\");\n");
		sb.append(js);
		return sb.toString();
	}
}
