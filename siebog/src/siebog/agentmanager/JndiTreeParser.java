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

package siebog.agentmanager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import siebog.utils.ContextFactory;

/**
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateless
@LocalBean
public class JndiTreeParser {
	private static final String INTF = "!" + Agent.class.getName();
	private static final String EXP = "java:jboss/exported/";
	private Context context;
	private Set<Class<? extends XjafAgent>> ignored;

	@PostConstruct
	public void postConstruct() {
		context = ContextFactory.get();
		ignored = new HashSet<>();
	}

	public List<AgentClass> parse() throws NamingException {
		List<AgentClass> result = new ArrayList<>();
		NamingEnumeration<NameClassPair> moduleList = context.list(EXP);
		while (moduleList.hasMore()) {
			String module = moduleList.next().getName();
			processModule(module, result);
		}
		return result;
	}

	private void processModule(String module, List<AgentClass> result) throws NamingException {
		NamingEnumeration<NameClassPair> agentList = context.list(EXP + "/" + module);
		while (agentList.hasMore()) {
			String ejbName = agentList.next().getName();
			AgentClass agClass = parseEjbNameIfValid(module, ejbName);
			if (agClass != null) {
				result.add(agClass);
			}
		}
	}

	private AgentClass parseEjbNameIfValid(String module, String ejbName) {
		if (ejbName != null && ejbName.endsWith(INTF)) {
			return parseEjbName(module, ejbName);
		}
		return null;
	}

	private AgentClass parseEjbName(String module, String ejbName) {
		ejbName = extractAgentName(ejbName);
		if (!ignored.contains(ejbName)) {
			String path = String.format("/%s/agents/xjaf", module);
			return new AgentClass(module, ejbName, path);
		}
		return null;
	}

	private String extractAgentName(String ejbName) {
		int n = ejbName.lastIndexOf(INTF);
		return ejbName.substring(0, n);
	}
}
