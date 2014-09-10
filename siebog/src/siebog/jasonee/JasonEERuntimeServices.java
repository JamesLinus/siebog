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

package siebog.jasonee;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServicesInfraTier;
import jason.runtime.Settings;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.managers.AgentInitArgs;
import siebog.xjaf.managers.AgentManager;
import siebog.xjaf.managers.RunningAgent;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class JasonEERuntimeServices implements RuntimeServicesInfraTier {
	private static final AgentManager agm = ObjectFactory.getAgentManager();

	@Override
	public String createAgent(String agName, String agSource, String agClass, List<String> archClasses,
			ClassParameters bbPars, Settings stts) throws Exception {
		AgentClass siebogAgClass = new AgentClass(agClass);

		AgentInitArgs args = new AgentInitArgs();
		args.put("source", agSource);

		AID aid = agm.startAgent(siebogAgClass, agName, args);
		return aid.getId();
	}

	@Override
	public void startAgent(String agName) {
		// nothing to do here
	}

	@Override
	public AgArch clone(jason.asSemantics.Agent source, List<String> archClasses, String agName) throws JasonException {
		throw new JasonException("Clone for Siebog is not implemented!");
	}

	@Override
	public Set<String> getAgentsNames() {
		List<RunningAgent> running = agm.getRunningAgents();
		Set<String> names = new HashSet<>(running.size());
		for (RunningAgent ag : running)
			names.add(ag.getAid().getId());
		return names;
	}

	@Override
	public int getAgentsQty() {
		return agm.getRunningAgents().size();
	}

	@Override
	public boolean killAgent(String agName, String byAg) {
		agm.stopAgent(new AID(agName));
		return true;
	}

	@Override
	public void stopMAS() throws Exception {
		// TODO Implement stopMAS()
	}
}
