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

import java.util.Arrays;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.agents.AID;
import siebog.agents.Agent;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class Initiator extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private int pendingProposals;

	@Override
	protected void onMessage(ACLMessage msg) {
		switch (msg.performative) {
		case REQUEST:
			AID[] participants = (AID[]) msg.contentObj;
			sendCfps(participants);
			pendingProposals = participants.length;
			break;
		case ACCEPT_PROPOSAL:
			--pendingProposals;
			if (pendingProposals == 0)
				;// agm().stopAgent(myAid);
			break;
		default:
			logger.info("Message not understood: " + msg);
		}
	}

	private void sendCfps(AID[] participants) {
		ACLMessage msg = new ACLMessage(Performative.CALL_FOR_PROPOSAL);
		msg.sender = myAid;
		msg.receivers.addAll(Arrays.asList(participants));
		msm().post(msg);
	}
}
