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
import java.util.List;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import siebog.core.Global;
import siebog.jasonee.control.ExecutionControl;
import siebog.jasonee.control.ExecutionControlBean;
import siebog.jasonee.control.UserExecutionControl;
import siebog.jasonee.environment.Environment;
import siebog.jasonee.environment.EnvironmentBean;
import siebog.jasonee.environment.UserEnvironment;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.managers.AgentInitArgs;
import siebog.xjaf.managers.AgentManager;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateless
@Remote(JasonEEStarter.class)
public class JasonEEStarterImpl implements JasonEEStarter {
	private JasonEEProject project;
	private MAS2JProject mas2j;
	private String remObjFactModule;
	private String remObjFactEjb;
	private RemoteObjectFactory remObjFact;
	private String ctrlName;
	private String envName;

	@Override
	public void start(JasonEEProject project) {
		this.project = project;
		mas2j = project.getMas2j();
		createRemObjFact();

		createExecutionControl();
		createEnvironment();

		createAgents();
	}

	private void createRemObjFact() {
		final List<AgentParameters> agents = mas2j.getAgents();
		for (AgentParameters agp : agents)
			if (agp.name.equals(RemoteObjectFactory.NAME)) {
				remObjFactModule = agp.getOption("module");
				remObjFactEjb = agp.getOption("object");
				remObjFact = ObjectFactory.getRemoteObjectFactory(remObjFactModule, remObjFactEjb);
				return;
			}
		throw new IllegalArgumentException("Need to specify the RemoteObjectFactory object.");
	}

	private void createExecutionControl() {
		final String lookup = "ejb:/" + Global.SERVER + "//" + ExecutionControlBean.class.getSimpleName() + "!"
				+ ExecutionControl.class.getName() + "?stateful";
		ExecutionControl ctrl = ObjectFactory.lookup(lookup, ExecutionControl.class);
		ctrlName = "ExecCtrl" + System.currentTimeMillis();
		ObjectFactory.getExecutionControlCache().put(ctrlName, ctrl);
		// create user's execution control
		ClassParameters userClass = mas2j.getControlClass();
		UserExecutionControl userExecCtrl = null;
		if (userClass != null) {
			try {
				userExecCtrl = remObjFact.createExecutionControl(userClass.getClassName());
				userExecCtrl.init(ctrlName, userClass.getParametersArray());
			} catch (Exception ex) {
				final String msg = "Unable to create user execution control " + userClass.getClassName();
				throw new IllegalStateException(msg, ex);
			}
		}
		ctrl.init(ctrlName, userExecCtrl);
	}

	private void createEnvironment() {
		final String lookup = "ejb:/" + Global.SERVER + "//" + EnvironmentBean.class.getSimpleName() + "!"
				+ Environment.class.getName();
		Environment env = ObjectFactory.lookup(lookup, Environment.class);
		envName = "Env" + System.currentTimeMillis();
		ObjectFactory.getEnvironmentCache().put(envName, env);

		// create user's environment
		UserEnvironment userEnv = null;
		ClassParameters userClass = mas2j.getEnvClass();
		if (userClass != null) {
			if (userClass.getClassName().equals("jason.environment.Environment"))
				userEnv = new UserEnvironment();
			else {
				try {
					userEnv = remObjFact.createEnvironment(userClass.getClassName());
				} catch (Exception ex) {
					final String msg = "Unable to create user environment " + userClass.getClassName();
					throw new IllegalStateException(msg, ex);
				}
			}
			userEnv.init(envName, userClass.getParametersArray());
		}

		env.init(userEnv);
	}

	private void createAgents() {
		AgentManager agm = ObjectFactory.getAgentManager();
		final List<AgentParameters> agents = mas2j.getAgents();
		for (AgentParameters agp : agents) {
			String runtimeName = agp.name;
			if (runtimeName.equals(RemoteObjectFactory.NAME))
				continue;
			for (int i = 0; i < agp.qty; i++) {
				if (agp.qty > 1)
					runtimeName += (i + 1);
				AgentClass agClass = new AgentClass(Global.SERVER, JasonEEAgent.class.getSimpleName());
				AgentInitArgs args = new AgentInitArgs();
				args.put("mas2jSource", project.getMas2jSource());
				args.put("agentName", agp.name);
				args.put("agentSource", project.getAgentSource(agp.name));
				args.put("remObjFactModule", remObjFactModule);
				args.put("remObjFactEjb", remObjFactEjb);
				args.put("envName", envName);
				args.put("execCtrlName", ctrlName);
				agm.startAgent(agClass, runtimeName, args);
			}
		}
	}
}
