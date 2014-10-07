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
 * 
 * Based on implementations of Centralised and JADE infrastructures 
 * in Jason 1.4.1. 
 * Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al. 
 * 
 * To contact the original authors:
 * http://www.inf.ufrgs.br/~bordini 
 * http://www.das.ufsc.br/~jomi
 */

package siebog.jasonee;

import jason.architecture.AgArch;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Agent;
import jason.asSemantics.Message;
import jason.asSyntax.Literal;
import jason.mas2j.AgentParameters;
import jason.runtime.RuntimeServicesInfraTier;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import siebog.jasonee.environment.ActionFeedbackMessage;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.managers.RunningAgent;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class JasonEEAgArch extends AgArch implements Serializable {
	private static final long serialVersionUID = 1L;
	private transient JasonEEAgent agent;
	private boolean running = true;
	// TODO ActionExec -> intention -> intendedMeans -> unif is not serializable
	private transient Map<String, ActionExec> scheduledActions;
	private long actionId;
	private Map<String, String> options;

	public final void init(String remObjFactModule, String remObjFactEjb, AgentParameters agp) throws Exception {
		options = Collections.synchronizedMap(new HashMap<String, String>());
		if (agp.getOptions() != null)
			options.putAll(agp.getOptions());
		scheduledActions = new HashMap<>();

		Agent.create(this, agp.agClass.getClassName(), agp.getBBClass(), agp.asSource.getAbsolutePath(),
				agp.getAsSetts(false, false));
		insertAgArch(this);

		RemoteObjectFactory remObjFact = ObjectFactory.getRemoteObjectFactory(remObjFactModule, remObjFactEjb);
		createCustomArchs(remObjFact, agp.getAgArchClasses());
	}

	public void reasoningCycle() {
		getTS().reasoningCycle();
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public boolean canSleep() {
		return isRunning() && !agent.hasMessages();
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
		if (running)
			return agent.getPercepts();
		return null;
	}

	@Override
	public void act(ActionExec action, List<ActionExec> feedback) {
		if (running) {
			String replyWith = "rw" + (++actionId);
			scheduledActions.put(replyWith, action);
			agent.scheduleAction(action.getActionTerm(), replyWith);
		}
	}

	public void onActionFeedback(ActionFeedbackMessage msg) {
		ActionExec action = scheduledActions.remove(msg.getUserData());
		if (action != null) {
			action.setResult(msg.isSuccess());
			getTS().getC().addFeedbackAction(action);
		}
	}

	@Override
	public void checkMail() {
		ACLMessage acl;
		while ((acl = agent.getNextMessage()) != null) {
			String ilForce = JasonMessage.getIlForce(acl);
			String sender = acl.sender.toString();
			String replyWith = acl.replyWith;
			String inReplyTo = acl.inReplyTo;
			Serializable content = JasonMessage.getJasonContent(acl);
			if (content != null) {
				Message jmsg = new Message(ilForce, sender, agent.getAid().toString(), content, replyWith);
				if (inReplyTo != null)
					jmsg.setInReplyTo(inReplyTo);
				getTS().getC().getMailBox().add(jmsg);
			}
		}
	}

	@Override
	public void sendMsg(Message m) throws Exception {
		ACLMessage acl = JasonMessage.toAclMessage(m);
		ObjectFactory.getMessageManager().post(acl);
	}

	@Override
	public void broadcast(Message m) throws Exception {
		ACLMessage acl = JasonMessage.toAclMessage(m);
		List<RunningAgent> list = ObjectFactory.getAgentManager().getRunningAgents();
		final AID myAid = agent.getAid();
		for (RunningAgent ra : list)
			if (!myAid.equals(ra.getAid()))
				acl.receivers.add(ra.getAid());
		ObjectFactory.getMessageManager().post(acl);
	}

	@Override
	public String getAgName() {
		return agent.getAid().toString();
	}

	@Override
	public RuntimeServicesInfraTier getRuntimeServices() {
		return null;
	}

	public JasonEEAgent getAgent() {
		return agent;
	}

	public void setAgent(JasonEEAgent agent) {
		this.agent = agent;
	}

	@SuppressWarnings("deprecation")
	private void createCustomArchs(RemoteObjectFactory remObjFact, List<String> archs) throws Exception {
		if (archs == null || archs.size() == 0)
			return;
		for (String agArchClass : archs) {
			JasonEEAgArch a = remObjFact.createAgArch(agArchClass);
			a.setTS(getTS()); // so a.init() can use TS
			a.setAgent(agent);
			a.setOptions(options);
			a.initAg(null, null, null, null); // for compatibility reasons
			insertAgArch(a);
			a.init();
		}
	}

	public Map<String, String> getOptions() {
		return options;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}
}
