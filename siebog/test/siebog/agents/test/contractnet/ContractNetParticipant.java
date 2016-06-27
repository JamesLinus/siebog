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

package siebog.agents.test.contractnet;

import java.security.SecureRandom;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import siebog.agents.Agent;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.utils.LoggerUtil;

/**
 * Contract net participant which will either make a random bid or refuse to bid.
 * Each participant has a 33% chance of not biding, as well as a 25% chance of issuing 
 * a failure if its bid is accepted.
 * 
 * @author <a href="nikola.luburic@uns.ac.rs">Nikola Luburic<a>
 */

@Stateful
@Remote(Agent.class)
public class ContractNetParticipant extends XjafAgent {
	private static final long serialVersionUID = 1L;

	@Override
	protected void onMessage(ACLMessage msg) {
		LoggerUtil.logMessage(msg, myAid);
		switch (msg.performative) {
		case CALL_FOR_PROPOSAL:
			handleCallForProposal(msg);
			break;
		case REJECT_PROPOSAL:
			handleRejectProposal();
			break;
		case ACCEPT_PROPOSAL:
			handleAcceptProposal(msg);
			break;
		default:
			LoggerUtil.log("Unexpected message: " + msg, true);
		}
	}

	private void handleCallForProposal(ACLMessage msg) {
		if(msg.replyBy < System.currentTimeMillis()) {
			LoggerUtil.log("ReplyBy time has passed, discarding message: " + msg, true);
		} else {
			createProposal(msg);
		}
	}

	private void handleRejectProposal() {
		LoggerUtil.log("Agent " + myAid.getStr() + " has noted that his bid was rejected.", true);
	}
	
	private void handleAcceptProposal(ACLMessage msg) {
		ACLMessage reply = null;
		int success = new SecureRandom().nextInt(4);
		
		if (success == 0) {
			reply = new ACLMessage(Performative.FAILURE);
			LoggerUtil.log("Agent " + myAid.getStr() + " has noted that his bid was accepted, but can't confirm his bid, resulting in a failure.", true);
		} else {
			reply = new ACLMessage(Performative.INFORM);
			LoggerUtil.log("Agent " + myAid.getStr() + " has noted that his bid was accepted, and will confirm bid.", true);
		}
		
		reply.sender = myAid;
		reply.receivers.add(msg.sender);
		msm().post(reply);
	}
	
	private void createProposal(ACLMessage msg) {
		SecureRandom rnd = new SecureRandom();
		int delay = (rnd.nextInt(3) + 1) * 3;
		
		ACLMessage reply = new ACLMessage(Performative.PROPOSE);
		reply.receivers.add(msg.sender);
		reply.sender = myAid;
		
		if(delay < 4) {
			LoggerUtil.log("Agent " + myAid.getStr() + " is not bidding.", true);
			reply.performative = Performative.REFUSE;
		} else {
			Integer bid = rnd.nextInt(40) + 20;
			LoggerUtil.log("Agent " + myAid.getStr() + " is bidding " + bid + ".", true);
			reply.content = bid.toString();
		}
		
		msm().post(reply, delay * 1000);
	}
}
