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

import jason.asSyntax.directives.DirectiveProcessor;
import jason.asSyntax.directives.Include;
import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;
import jason.mas2j.parser.mas2j;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.ejb.EJB;
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

	@Override
	protected void onInit(Map<String, String> args) {
		final String agentNameInMas2j = args.get("agentNameInMas2j");
		final String fileName = args.get("mas2jFileName");
		AgentParameters agp = getAgentParams(agentNameInMas2j, fileName);
		arch = new SiebogAgArch();
		try {
			arch.init(agp);
		} catch (Exception ex) {
			throw new IllegalStateException("Error during agent architecture initialization.", ex);
		}

		final Runnable task = new Runnable() {
			@Override
			public void run() {
				arch.reasoningCycle();
			}
		};
		executor.executeRepeating(task, 500, 500, TimeUnit.MILLISECONDS);
	}

	private AgentParameters getAgentParams(String agentName, String mas2jFileName) {
		try (InputStream in = new FileInputStream(mas2jFileName)) {
			mas2j parser = new mas2j(in);
			MAS2JProject project = parser.mas();
			project.setupDefault();

			project.registerDirectives();
			((Include) DirectiveProcessor.getDirective("include")).setSourcePath(project.getSourcePaths());

			AgentParameters agp = project.getAg(mas2jFileName);
			if (agp == null)
				throw new IllegalArgumentException("Agent " + agentName + " is not defined.");
			agp.fixSrc(project.getSourcePaths(), null);
			return agp;
		} catch (IOException | ParseException ex) {
			throw new IllegalArgumentException("Error while reading " + mas2jFileName, ex);
		}
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		arch.onMessage(msg);
	}
}
