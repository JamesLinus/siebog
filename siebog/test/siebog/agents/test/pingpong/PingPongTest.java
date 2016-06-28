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

package siebog.agents.test.pingpong;

import siebog.agentmanager.AID;
import siebog.agentmanager.Agent;
import siebog.agentmanager.AgentClass;
import siebog.agents.test.TestClientBase;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.Performative;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 * @author <a href="nikola.luburic@uns.ac.rs">Nikola Luburic</a>
 */
public class PingPongTest extends TestClientBase {
	
	@Override
	public void test() {
		AID pingAid = agm.startServerAgent(new AgentClass(Agent.SIEBOG_MODULE, Ping.class.getSimpleName()), "Ping", null);
		String pongName = "Pong";
		agm.startServerAgent(new AgentClass(Agent.SIEBOG_MODULE, Pong.class.getSimpleName()), pongName, null);

		ACLMessage message = new ACLMessage(Performative.REQUEST);
		message.receivers.add(pingAid);
		message.content = pongName;
		message.replyTo = testAgentAid;
		msm.post(message);
	}

	public static void main(String[] args) {
		new PingPongTest().test();
	}
}
