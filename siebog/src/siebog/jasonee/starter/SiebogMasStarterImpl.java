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

package siebog.jasonee.starter;

import jason.asSyntax.directives.DirectiveProcessor;
import jason.asSyntax.directives.Include;
import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;
import jason.mas2j.parser.mas2j;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import siebog.jasonee.SiebogEnvironment;
import siebog.utils.ManagerFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.managers.AgentManager;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateless
@Remote(SiebogMasStarter.class)
public class SiebogMasStarterImpl implements SiebogMasStarter {
	private static final AgentManager agm = ManagerFactory.getAgentManager();

	@Override
	public void start(String projectFileName) {
		MAS2JProject project = loadProject(projectFileName);
		// create environment
		SiebogEnvironment env = new SiebogEnvironment();
		createAgs(project, env);
		createController();
		startAgs();
		startSyncMode();
	}

	private MAS2JProject loadProject(String fileName) {
		try (InputStream in = new FileInputStream(fileName)) {
			mas2j parser = new mas2j(in);
			MAS2JProject project = parser.mas();

			// TODO Check if I need to call this project initialization methods
			// project.setupDefault();
			// project.registerDirectives();
			// ((Include)DirectiveProcessor.getDirective("include")).setSourcePath(project.getSourcePaths());

			// project.fixAgentsSrc(urlPrefix);

			return project;
		} catch (IOException | ParseException ex) {
			throw new IllegalArgumentException("Error while loading " + fileName, ex);
		}
	}

	private void createAgs(MAS2JProject project, SiebogEnvironment env) {
		final List<AgentParameters> agents = project.getAgents();
		for (AgentParameters agp : agents) {
			for (int i = 0; i < agp.qty; i++) {
				String runtimeName = agp.name;
				if (agp.qty > 1)
					runtimeName += (i + 1);
				AgentClass agClass = new AgentClass(agp.agClass.getClassName());
				// TODO Where are the agent initialization arguments?
				AID aid = agm.startAgent(agClass, runtimeName, null);
			}

		}
	}
}
