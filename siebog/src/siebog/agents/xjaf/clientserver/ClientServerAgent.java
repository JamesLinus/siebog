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

package siebog.agents.xjaf.clientserver;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.PlatformId;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.Agent;
import siebog.xjaf.core.XjafAgent;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;
import siebog.xjaf.radigostlayer.RadigostAgent;

/**
 * An agent that tests the communication between the server and Radigost clients.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class ClientServerAgent extends XjafAgent {
	private static final long serialVersionUID = 1L;

	@Override
	protected void onMessage(ACLMessage msg) {
		if (msg.performative == Performative.REQUEST)
			sendMessageToClient(msg.sender.toString());
		else
			replyToOriginalSender(msg);
	}

	private void sendMessageToClient(String replyWith) {
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.sender = myAid;
		AID client = new AID("CSClient", "ClientServer", PlatformId.RADIGOST, RadigostAgent.AGENT_CLASS);
		msg.receivers.add(client);
		msg.replyWith = replyWith;
		msm().post(msg);
	}

	private void replyToOriginalSender(ACLMessage msg) {
		ACLMessage reply = new ACLMessage(Performative.INFORM);
		reply.receivers.add(new AID(msg.inReplyTo));
		msm().post(reply);
	}
}
