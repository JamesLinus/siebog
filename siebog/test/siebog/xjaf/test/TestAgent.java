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

package siebog.xjaf.test;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.xjaf.core.Agent;
import siebog.xjaf.core.XjafAgent;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.managers.AgentInitArgs;

/**
 * All messages sent to this agent will be forwarded to the remote RMI service.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class TestAgent extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private TestAgentListener listener;

	@Override
	protected void onInit(AgentInitArgs args) {
		try {
			String remoteHost = args.get("remoteHost");
			Registry reg = LocateRegistry.getRegistry(remoteHost);
			listener = (TestAgentListener) reg.lookup("TestAgentListener");
		} catch (Exception ex) {
			throw new IllegalArgumentException("Cannot connect to the remote RMI service.", ex);
		}
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		if (listener != null)
			listener.onMessage(msg);
	}

}
