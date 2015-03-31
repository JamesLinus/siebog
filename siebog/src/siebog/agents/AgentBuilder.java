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

package siebog.agents;

import java.util.HashSet;
import java.util.Set;
import siebog.core.Global;
import siebog.utils.ObjectFactory;

/**
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class AgentBuilder {
	private String module;
	private AgentClass agClass;
	private AgentInitArgs args;
	private String lastArgName;
	private String name;
	private boolean radigost;

	private AgentBuilder(String module) {
		this.module = module;
	}

	public static AgentBuilder siebog() {
		return module(Global.SIEBOG_MODULE);
	}

	public static AgentBuilder module(String name) {
		AgentBuilder ab = new AgentBuilder(name);
		return ab;
	}

	public AgentBuilder ejb(Class<? extends XjafAgent> ejbClass) {
		if (agClass != null) {
			throw new IllegalStateException("Agent class already set.");
		}
		agClass = new AgentClass(module, ejbClass.getSimpleName());
		return this;
	}

	public AgentBuilder arg(String name) {
		if (lastArgName != null) {
			throw new IllegalStateException("Missing value for argument " + lastArgName);
		}
		lastArgName = name;
		return this;
	}

	public AgentBuilder value(String value) {
		if (lastArgName == null) {
			throw new IllegalStateException("Missing argument name.");
		}
		if (args == null) {
			args = new AgentInitArgs();
		}
		args.put(lastArgName, value);
		lastArgName = null;
		return this;
	}

	public AgentBuilder name(String name) {
		this.name = name;
		return this;
	}

	public AgentBuilder randomName() {
		return name(getRandomName());
	}

	public AgentBuilder client() {
		radigost = true;
		return this;
	}

	public AgentBuilder server() {
		radigost = false;
		return this;
	}

	public AID start() {
		if (agClass == null) {
			throw new IllegalStateException("Missing agent class.");
		}
		if (name == null) {
			throw new IllegalStateException("Missing agent name.");
		}
		return startAgent(name);
	}

	public Set<AID> startNInstances(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("Invalid number of agent instances: " + n);
		}
		if (name != null) {
			throw new IllegalStateException("Cannot set the name when creating multiple agents.");
		}
		Set<AID> set = new HashSet<>();
		for (int i = 0; i < n; i++) {
			AID aid = startAgent(getRandomName());
			set.add(aid);
		}
		return set;
	}

	private String getRandomName() {
		return "Agent-" + (int) (Math.random() * Integer.MAX_VALUE);
	}

	private AID startAgent(String name) {
		if (radigost) {
			return startClientAgent(name);
		}
		return startServerAgent(name);
	}

	private AID startClientAgent(String name) {
		AgentManager agm = ObjectFactory.getAgentManager();
		return agm.startClientAgent(agClass, name, args);
	}

	private AID startServerAgent(String name) {
		AgentManager agm = ObjectFactory.getAgentManager();
		return agm.startServerAgent(agClass, name, args);
	}
}
