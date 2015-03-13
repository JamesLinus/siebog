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

package siebog.agents.test.hacontractnet;

import siebog.SiebogClient;
import siebog.agents.AID;
import siebog.agents.AgentClass;
import siebog.core.Global;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.utils.ObjectFactory;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class HAContractNet {
	private static final int NUM_PARTICIPANTS = 16;

	public static void main(String[] args) {
		SiebogClient.connect("localhost");
		AID[] participants = createParticipants();
		AID initiator = createInitiator();
		start(initiator, participants);
	}

	private static AID[] createParticipants() {
		AID[] aids = new AID[NUM_PARTICIPANTS];
		AgentClass pcls = new AgentClass(Global.SIEBOG_MODULE, Participant.class.getSimpleName());
		for (int i = 0; i < NUM_PARTICIPANTS; i++)
			aids[i] = ObjectFactory.getAgentManager().startAgent(pcls, "P" + i, null);
		return aids;
	}

	private static AID createInitiator() {
		AgentClass icls = new AgentClass(Global.SIEBOG_MODULE, Initiator.class.getSimpleName());
		return ObjectFactory.getAgentManager().startAgent(icls, "I", null);
	}

	private static void start(AID initiator, AID[] participants) {
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.receivers.add(initiator);
		msg.contentObj = participants;
		ObjectFactory.getMessageManager().post(msg);
	}
}
