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
import jason.mas2j.ClassParameters;
import jason.mas2j.MAS2JProject;
import java.io.File;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.hornetq.utils.json.JSONArray;
import scala.actors.threadpool.Arrays;
import siebog.core.Global;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.managers.AgentInitArgs;
import siebog.xjaf.managers.AgentManager;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateless
@Remote(JasonEEStarter.class)
@LocalBean
@Path("/jasonee")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JasonEEStarterImpl implements JasonEEStarter {
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Override
	public void start(@FormParam("mas2jFileName") String mas2jFileName) {
		MAS2JProject project = Mas2jProjectFactory.load(new File(mas2jFileName));
		String env = createEnvironment(project.getEnvClass());
		createAgents(mas2jFileName, project, env);
	}

	private String createEnvironment(ClassParameters envClass) {
		JasonEEEnvironment env = ObjectFactory.getJasonEEEnvironment();
		env.init(envClass.getClassName(), envClass.getParametersArray());
		return ObjectFactory.getJasonEEApp().putEnv(env);
	}

	private String getEnvParamsAsString(String[] params) {
		JSONArray json = new JSONArray(Arrays.asList(params));
		return json.toString();
	}

	private void createAgents(String mas2jFileName, MAS2JProject project, String env) {
		AgentManager agm = ObjectFactory.getAgentManager();
		final List<AgentParameters> agents = project.getAgents();
		for (AgentParameters agp : agents) {
			for (int i = 0; i < agp.qty; i++) {
				String runtimeName = agp.name;
				if (agp.qty > 1)
					runtimeName += (i + 1);

				AgentClass agClass = new AgentClass(Global.SERVER, JasonEEAgent.class.getSimpleName());
				AgentInitArgs args = new AgentInitArgs();
				args.put("agentName", agp.name);
				args.put("mas2jFileName", mas2jFileName);
				args.put("env", env);
				agm.startAgent(agClass, runtimeName, args);
			}
		}
	}

	private void createController() {
	}

	private void startAgs() {
	}
}
