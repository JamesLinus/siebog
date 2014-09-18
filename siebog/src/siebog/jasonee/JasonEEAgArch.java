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

import jason.architecture.AgArch;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Agent;
import jason.asSemantics.Message;
import jason.asSyntax.Literal;
import jason.mas2j.AgentParameters;
import jason.runtime.RuntimeServicesInfraTier;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import siebog.jasonee.environment.ActionFeedbackMessage;
import siebog.jasonee.environment.Environment;
import siebog.utils.ObjectFactory;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.managers.AgentInitArgs;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class JasonEEAgArch extends AgArch {
	private JasonEEAgent agent;
	private Deque<ACLMessage> mailbox = new LinkedList<>();
	private boolean running;
	private String envName;
	private Map<String, ActionExec> scheduledActions;
	private long actionId;

	public void init(AgentInitArgs args, AgentParameters agp, JasonEEAgent agent, String envName) throws Exception {
		this.agent = agent;
		this.envName = envName;
		scheduledActions = new HashMap<>();

		Agent.create(this, agp.agClass.getClassName(), agp.getBBClass(), agp.asSource.getAbsolutePath(),
				agp.getAsSetts(false, false));
		insertAgArch(this);

		final String module = args.get("remObjFactModule");
		final String ejb = args.get("remObjFactEjb");
		RemoteObjectFactory remObjFact = ObjectFactory.getRemoteObjectFactory(module, ejb);
		createCustomArchs(remObjFact, agp.getAgArchClasses());

		running = true;
	}

	public void reasoningCycle() {
		getTS().reasoningCycle();
	}

	public void onMessage(ACLMessage msg) {
		mailbox.add(msg);
		wake();
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public boolean canSleep() {
		return isRunning() && mailbox.isEmpty();
	}

	@Override
	public void sleep() {
		agent.sleep();
	}

	@Override
	public void wake() {
		agent.wakeUp();
	}

	@Override
	public void stop() {
		running = false;
		getTS().getAg().stopAg();
		ObjectFactory.getAgentManager().stopAgent(agent.getAid());
		super.stop();
	}

	@Override
	public List<Literal> perceive() {
		if (!running)
			return null;
		return env().getPercepts(agent.getAid());
	}

	@Override
	public void act(ActionExec action, List<ActionExec> feedback) {
		if (!running)
			return;
		String replyWith = "rw" + (++actionId);
		scheduledActions.put(replyWith, action);
		env().scheduleAction(agent.getAid(), action.getActionTerm(), replyWith);
	}

	public void onActionFeedback(ActionFeedbackMessage msg) {
		ActionExec action = scheduledActions.remove(msg.getUserData());
		if (action != null) {
			action.setResult(Boolean.parseBoolean(msg.content));
			getTS().getC().addFeedbackAction(action);
		}
	}

	@Override
	public void checkMail() {
	}

	@Override
	public void sendMsg(Message m) throws Exception {
	}

	@Override
	public void broadcast(Message m) throws Exception {
	}

	@Override
	public String getAgName() {
		return agent.getAid().toString();
	}

	@Override
	public RuntimeServicesInfraTier getRuntimeServices() {
		return new JasonEERuntimeServices();
	}

	private Environment env() {
		return ObjectFactory.getJasonEEApp().getEnv(envName);
	}

	@SuppressWarnings("deprecation")
	private void createCustomArchs(RemoteObjectFactory remObjFact, List<String> archs) throws Exception {
		if (archs == null || archs.size() == 0)
			return;
		for (String agArchClass : archs) {
			AgArch a = remObjFact.createAgArch(agArchClass);
			a.setTS(getTS()); // so a.init() can use TS
			a.initAg(null, null, null, null); // for compatibility reasons
			insertAgArch(a);
			a.init();
		}
	}
}
