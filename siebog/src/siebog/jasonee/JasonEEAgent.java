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
import java.util.logging.Level;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.jasonee.intf.JasonEEExecutionControl;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.Agent;
import siebog.xjaf.core.XjafAgent;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.managers.AgentInitArgs;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class JasonEEAgent extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private String execCtrlName;
	private JasonEEExecutionControl execCtrl;
	private JasonEEAgArch arch;
	private boolean syncMode;
	private int asyncCycleNum;
	private boolean sleeping = false;

	@Override
	protected void onInit(AgentInitArgs args) {
		final String agentName = args.get("agentName");
		final String mas2jFileName = args.get("mas2jFileName");
		final String envName = args.get("envName");
		execCtrlName = args.get("execCtrlName");
		AgentParameters agp = getAgentParams(agentName, new File(mas2jFileName));
		arch = new JasonEEAgArch();
		try {
			arch.init(agp, this, envName);
		} catch (Exception ex) {
			final String msg = "Error while initializing agent architecture.";
			logger.log(Level.SEVERE, msg, ex);
			throw new IllegalStateException(msg, ex);
		}
		syncMode = arch.getTS().getSettings().isSync();
		wakeUp();
	}

	private AgentParameters getAgentParams(String agentName, File mas2jFile) {
		MAS2JProject project = Mas2jProjectFactory.load(mas2jFile);
		AgentParameters agp = project.getAg(agentName);
		if (agp == null)
			throw new IllegalArgumentException("Agent " + agentName + " is not defined.");
		// TODO Use the correct urlPrefix here
		// agp.fixSrc(project.getSourcePaths(), ???);
		agp.asSource = new File(mas2jFile.getParent(), project.getSourcePaths().get(0) + "/" + agp.asSource.getName());
		return agp;
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		if (msg instanceof ReasoningCycleMessage)
			onSyncCycle(((ReasoningCycleMessage) msg).getCycleNum());
		else if (msg instanceof ActionFeedbackMessage)
			arch.onActionFeedback((ActionFeedbackMessage) msg);
		else
			arch.onMessage(msg);
	}

	@Override
	protected boolean onHeartbeat(String content) {
		if (!sleeping) {
			++asyncCycleNum;
			arch.reasoningCycle();
			return true;
		}
		return false;
	}

	private void onSyncCycle(int cycleNum) {
		arch.reasoningCycle();
		boolean isBreakpoint;
		try {
			isBreakpoint = arch.getTS().getC().getSelectedOption().getPlan().hasBreakpoint();
		} catch (NullPointerException ex) {
			isBreakpoint = false;
		}
		execCtrl().agentCycleFinished(myAid, isBreakpoint, cycleNum);
	}

	public void sleep() {
		if (syncMode)
			execCtrl.remAgent(myAid);
		else
			sleeping = true;
	}

	public void wakeUp() {
		if (syncMode)
			execCtrl.addAgent(myAid);
		else {
			sleeping = false;
			registerHeartbeat();
		}
	}

	public ACLMessage ask(ACLMessage msg) {
		return null;
	}

	private JasonEEExecutionControl execCtrl() {
		if (execCtrl == null)
			execCtrl = ObjectFactory.getJasonEEApp().getExecCtrl(execCtrlName);
		return execCtrl;
	}
}
