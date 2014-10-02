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

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.mas2j.AgentParameters;
import jason.mas2j.MAS2JProject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.core.FileUtils;
import siebog.jasonee.control.ExecutionControl;
import siebog.jasonee.control.ReasoningCycleMessage;
import siebog.jasonee.control.ReasoningCycleTimeout;
import siebog.jasonee.environment.ActionFeedbackMessage;
import siebog.jasonee.environment.Environment;
import siebog.jasonee.environment.EnvironmentChangedMessage;
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
	private transient ExecutionControl control;
	private String envName;
	private transient Environment env;
	private JasonEEAgArch arch;
	private boolean syncMode;
	private int asyncCycleNum;
	private boolean sleeping = true;
	private int cycleNum;
	private AgentInitArgs args;
	private BlockingDeque<ACLMessage> mailbox = new LinkedBlockingDeque<>();

	@Override
	protected void onInit(AgentInitArgs args) {
		this.args = args;
		execCtrlName = args.get("execCtrlName");
		envName = args.get("envName");
		arch = new JasonEEAgArch();
		initArch();
		syncMode = arch.getTS().getSettings().isSync();
		wakeUp();
	}

	private void initArch() {
		AgentParameters agp = null;
		try {
			File mas2jFile = FileUtils.createTempFile(args.get("mas2jSource"));
			agp = getAgentParams(args.get("agentName"), mas2jFile);
			agp.asSource = FileUtils.createTempFile(args.get("agentSource"));
		} catch (IOException ex) {
			throw new IllegalStateException("Cannot store agent source in a temporary file.", ex);
		}

		try {
			arch.setAgent(this);
			arch.init(args.get("remObjFactModule"), args.get("remObjFactEjb"), agp);
		} catch (Exception ex) {
			final String msg = "Error while initializing agent architecture.";
			logger.log(Level.SEVERE, msg, ex);
			throw new IllegalStateException(msg, ex);
		}
	}

	private AgentParameters getAgentParams(String agentName, File mas2jFile) {
		MAS2JProject project = JasonEEProject.loadFromFile(mas2jFile).getMas2j();
		AgentParameters agp = project.getAg(agentName);
		if (agp == null)
			throw new IllegalArgumentException("Agent " + agentName + " is not defined.");
		return agp;
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		wakeUp();
		if (msg instanceof ReasoningCycleMessage)
			performCycle(((ReasoningCycleMessage) msg).cycleNum);
		else if (msg instanceof ReasoningCycleTimeout) {
			if (syncMode && cycleNum <= ((ReasoningCycleTimeout) msg).cycleNum)
				executionControl().agentCycleFinished(myAid, isBreakpoint(), cycleNum);
		} else if (msg instanceof ActionFeedbackMessage)
			arch.onActionFeedback((ActionFeedbackMessage) msg);
		else if (msg instanceof EnvironmentChangedMessage)
			; // nothing, it woke me up
		else
			mailbox.add(msg);
	}

	@Override
	protected boolean onHeartbeat(String content) {
		if (!sleeping) {
			++asyncCycleNum;
			performCycle(asyncCycleNum);
			return true;
		}
		return false;
	}

	private void performCycle(int cycleNum) {
		this.cycleNum = cycleNum;
		try {
			if (arch.getTS() == null) {
				logger.warning("Re-initializing the agent architecture.");
				initArch();
			}
			arch.reasoningCycle();
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Error in reasoning cycle.", ex);
		} finally {
			if (syncMode)
				executionControl().agentCycleFinished(myAid, isBreakpoint(), cycleNum);
		}
	}

	private boolean isBreakpoint() {
		try {
			return arch.getTS().getC().getSelectedOption().getPlan().hasBreakpoint();
		} catch (NullPointerException ex) {
			return false;
		}
	}

	public void sleep() {
		if (!sleeping) {
			sleeping = true;
			if (syncMode)
				executionControl().removeAgent(myAid);
		}
	}

	public void wakeUp() {
		if (sleeping) {
			sleeping = false;
			if (syncMode)
				executionControl().addAgent(myAid);
			else
				registerHeartbeat();
		}
	}

	public ACLMessage getNextMessage() {
		return mailbox.poll();
	}

	public boolean hasMessages() {
		return mailbox.size() > 0;
	}

	@Override
	public void onTerminate() {
		if (syncMode)
			executionControl().removeAgent(myAid);
	}

	public void scheduleAction(Structure action, String replyWith) {
		env().scheduleAction(myAid, action, replyWith);
	}

	public List<Literal> getPercepts() {
		return env().getPercepts(myAid);
	}

	private ExecutionControl executionControl() {
		if (control == null)
			control = ObjectFactory.getExecutionControlCache().get(execCtrlName);
		return control;
	}

	private Environment env() {
		if (env == null)
			env = ObjectFactory.getEnvironmentCache().get(envName);
		return env;
	}
}
