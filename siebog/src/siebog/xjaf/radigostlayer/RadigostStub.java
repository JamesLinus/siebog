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

package siebog.xjaf.radigostlayer;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import siebog.agents.Agent;
import siebog.agents.AgentClass;
import siebog.agents.AgentInitArgs;
import siebog.agents.XjafAgent;
import siebog.core.Global;
import siebog.interaction.ACLMessage;

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
	public static final AgentClass AGENT_CLASS = new AgentClass(Global.SIEBOG_MODULE, RadigostStub.class.getSimpleName());
	@Inject
	@Default
	private Event<ACLMessage> webSocketEvent;

	@Override
	protected void onInit(AgentInitArgs args) {
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		webSocketEvent.fire(msg);
	}
}
