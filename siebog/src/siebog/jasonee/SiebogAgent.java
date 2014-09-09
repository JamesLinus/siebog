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

import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import java.io.File;
import java.util.Map;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.xjaf.core.Agent;
import siebog.xjaf.core.XjafAgent;
import siebog.xjaf.fipa.ACLMessage;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class SiebogAgent extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private SiebogAgArch arch;
	private long repeatingId;

	@Override
	protected void onInit(Map<String, String> args) {
		final String agentName = args.get("agentName");
		final String mas2jFileName = args.get("mas2jFileName");
		AgentParameters agp = getAgentParams(agentName, new File(mas2jFileName));
		arch = new SiebogAgArch();
		try {
			arch.init(agp);
		} catch (Exception ex) {
			throw new IllegalStateException("Error during agent architecture initialization.", ex);
		}
		repeatingId = executor.registerHeartbeat(myAid, 500);
	}

	private AgentParameters getAgentParams(String agentName, File mas2jFile) {
		MAS2JProject project = Mas2jProjectFactory.load(mas2jFile);
		AgentParameters agp = project.getAg(agentName);
		if (agp == null)
			throw new IllegalArgumentException("Agent " + agentName + " is not defined.");
		// TODO Use the correct urlPrefix here
		// agp.fixSrc(project.getSourcePaths(), "/home/dejan/dev/siebog/jason_examples/src/asl/");
		agp.asSource = new File("/home/dejan/dev/siebog/jason_examples/src/asl/sample_agent.asl");
		return agp;
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		if (executor.isHearbeat(msg))
			arch.reasoningCycle();
		else
			arch.onMessage(msg);
	}

	@Override
	protected void onTerminate() {
		executor.cancelRepeating(repeatingId);
	}
}
