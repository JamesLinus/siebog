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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.agents.AID;
import siebog.agents.Agent;
import siebog.agents.AgentClass;
import siebog.agents.AgentInitArgs;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;

/**
 * Example of a ping agent.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class Ping extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(Ping.class);
	private String nodeName;

	@Override
	protected void onInit(AgentInitArgs args) {
		nodeName = getNodeName();
		LOG.info("Ping created on {}.", nodeName);
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		System.out.println("Message to Ping: " + msg);
		if (msg.performative == Performative.REQUEST) { // inital request
			// send a request to the Pong agent
			AgentClass agClass = new AgentClass(Agent.SIEBOG_MODULE, Pong.class.getSimpleName());
			AID pongAid = new AID(msg.content, agClass);
			ACLMessage msgToPong = new ACLMessage(Performative.REQUEST);
			msgToPong.sender = myAid;
			msgToPong.receivers.add(pongAid);
			msm().post(msgToPong);
		} else if (msg.performative == Performative.INFORM) {
			// wait for the message
			// ACLMessage msgFromPong = receiveWait(0);
			ACLMessage msgFromPong = msg;
			Map<String, Serializable> args = new HashMap<>(msgFromPong.userArgs);
			args.put("pingCreatedOn", nodeName);
			args.put("pingWorkingOn", getNodeName());

			// print info
			LOG.info("Ping-Pong interaction details:");
			for (Entry<String, Serializable> e : args.entrySet())
				LOG.info("{} {}", e.getKey(), e.getValue());

			// reply to the original sender (if any)
			if (msg.canReplyTo()) {
				ACLMessage reply = msg.makeReply(Performative.INFORM);
				reply.userArgs = args;
				msm().post(reply);
			}
		}
	}
}