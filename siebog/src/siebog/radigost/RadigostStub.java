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

package siebog.radigost;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.script.Invocable;
import javax.script.ScriptException;

import siebog.agentmanager.Agent;
import siebog.agentmanager.AgentClass;
import siebog.agentmanager.AgentInitArgs;
import siebog.agentmanager.XjafAgent;
import siebog.messagemanager.ACLMessage;

/**
 * Stub representation of a Radigost agent. Any messages sent to this instance will be forwarded to
 * the client agent.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class RadigostStub extends XjafAgent {
	private static final long serialVersionUID = 1L;
	// for speed purposes
	public static final AgentClass AGENT_CLASS = AgentClass.forSiebogEjb(RadigostStub.class);
	private boolean emptyStub;
	private transient Invocable invocable;
	private transient Object jsAgent;
	@Inject
	@Default
	private Event<ACLMessage> webSocketEvent;

	@Override
	protected void onInit(AgentInitArgs args) {
		if (args == null || args.get("url", null) == null) {
			emptyStub = true;
		} else {
			emptyStub = false;
			loadJsAgent(args.get("url", ""), args.get("state", "{}"));
		}
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		if (emptyStub) {
			webSocketEvent.fire(msg);
		} else {
			sendMsgToJsAgent(msg);
		}
	}

	private void loadJsAgent(String url, String state) {
		try {
			invocable = new ScriptLoader().load(url, state);
			jsAgent = invocable.invokeFunction("getAgentInstance");
		} catch (ScriptException | NoSuchMethodException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	private void sendMsgToJsAgent(ACLMessage msg) {
		String jsonMsg = msg.toString();
		try {
			invocable.invokeMethod(jsAgent, "onMessage", jsonMsg);
		} catch (NoSuchMethodException | ScriptException ex) {
			throw new IllegalStateException(ex);
		}
	}
}
