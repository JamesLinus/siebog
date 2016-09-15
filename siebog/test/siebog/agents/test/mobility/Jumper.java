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

package siebog.agents.test.mobility;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import siebog.agentmanager.Agent;
import siebog.agentmanager.AgentInitArgs;
import siebog.agentmanager.XjafAgent;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.Performative;
import siebog.utils.LoggerUtil;

/**
 * Example of agent mobility.
 *
 * @author <a href="nikol.luburic@uns.ac.rs">Nikola Luburic</a>
 */
@Stateful
@Remote(Agent.class)
public class Jumper extends XjafAgent {
	private static final long serialVersionUID = 1L;
	
	private int jumpCounter;
	private String home;

	@Override
	protected void onInit(AgentInitArgs args) {
		jumpCounter = 0;
		LoggerUtil.log("Jumper created with counter " + jumpCounter, true);
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		if(msg.performative == Performative.REQUEST) {
			this.home = msg.content.split(" ")[1];
			jumpCounter++;
			LoggerUtil.log("Jumper REQUEST with counter " + jumpCounter, true);
			agm().move(myAid, msg.content.split(" ")[0]);
		} else if(msg.performative == Performative.RESUME) {
			jumpCounter++;
			LoggerUtil.log("Jumper RESUME with counter " + jumpCounter, true);
			agm().move(myAid, home);
		}
	}
}