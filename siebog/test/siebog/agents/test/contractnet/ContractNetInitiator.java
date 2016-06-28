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

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import siebog.agentmanager.AID;
import siebog.agentmanager.Agent;
import siebog.agentmanager.XjafAgent;
import siebog.agents.test.contractnet.ContractNetConstants;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.Performative;
import siebog.utils.LoggerUtil;
import siebog.utils.ObjectFactory;

/**
 * The initiator class issues a call for proposal by contacting all the participants. 
 * Following the contract net protocol, bids are received and the lowest bid is accepted. 
 * The accepted agent is contacted to confirm the bid, while the other participants are 
 * notified that their bid is rejected.
 * 
 * If some participants don't reply with a bid by the end of the predetermined duration, 
 * a retry is issued in order to guarantee that a message wasn't lost in transit due to 
 * node failure. The number of retries and their duration are defined using the 
 * ContractNetConstants singleton session bean.
 * 
 * @author <a href="nikola.luburic@uns.ac.rs">Nikola Luburic<a>
 */

@Stateful
@Remote(Agent.class)
public class ContractNetInitiator extends XjafAgent {
	private static final long serialVersionUID = 1L;
	
	private int retries = 0;
	
	private boolean done = true;
	
	private Integer bestProposal;
	private AID bestProposalAgent;
	
	private List<AID> participants;
	
	@Override
	protected void onMessage(ACLMessage msg) {
		LoggerUtil.logMessage(msg, myAid);
		if(msg.performative == Performative.REQUEST) {
			if(done) {
				createCallForProposal(msg);
			} else {
				LoggerUtil.log("A contract net is already in process, discarding message: " + msg, true);
			}
		}
		else if(!done) {
			switch (msg.performative) {
			case PROPOSE:
				handleProposal(msg);
				break;
			case REFUSE:
				handleRefusal(msg);
				break;
			case INFORM:
				finished(msg);
				break;
			case FAILURE:
				handleFailure();
				break;
			default:
				LoggerUtil.log("Unexpected message: " + msg, true);
			}
		} else if(msg.performative != Performative.INFORM) {
			LoggerUtil.log("Received message after completion: " + msg, true);
		}
	}

	private void createCallForProposal(ACLMessage msg) {
		done = false;
		List<AID> runningAgents = ObjectFactory.getAgentManager().getRunningAgents();
		participants = new ArrayList<AID>();
		for(AID agent : runningAgents) {
			if(agent.getAgClass().toString().contains("ContractNetParticipant")) {
				participants.add(agent);
			}
		}
		long seconds = 10;
		try {
			seconds = Long.parseLong(msg.content);
		} catch(NumberFormatException e) {
			LoggerUtil.log("No or incorrect time of execution specified. Time of execution will be 10 seconds.", true);
		} finally {
			sendCallForProposalEndMessage(seconds);			
			sendCallForProposal(seconds);
		}
	}

	private void handleProposal(ACLMessage msg) {
		try {
			Integer receivedProposal = Integer.parseInt(msg.content);
			if(bestProposal == null) {
				bestProposal = receivedProposal;
				bestProposalAgent = msg.sender;
			}
			else if(receivedProposal < bestProposal) {
				bestProposal = receivedProposal;
				bestProposalAgent = msg.sender;
			}
			LoggerUtil.log("Recieved bid from agent " + msg.sender.getStr() + " of value " + msg.content + ".", true);
		} catch(NumberFormatException e) {
			LoggerUtil.log("Incorrect proposal received in message: " + msg, true);
		} finally {
			removeParticipant(msg.sender);
		}
	}

	private void handleRefusal(ACLMessage msg) {
		LoggerUtil.log("Recieved refusal from agent " + msg.sender.getStr() + ".", true);
		removeParticipant(msg.sender);
	}

	private void finished(ACLMessage msg) {
		if(msg != null && !msg.sender.getStr().equals(myAid.getStr())) {
			LoggerUtil.log("The contract net process has finished successfully after confirming the lowest bid.");
			done = true;
		} else if(msg == null || retries == ContractNetConstants.NUM_OF_RETRIES) {
			if(bestProposalAgent != null) {
				handleAcceptance();
			} else {
				handleFailure();
			}
		} else {
			retries++;
			LoggerUtil.log("The contractnet process has reached its timeout, but not all participants have responded.\nInitiating retry number " + retries + ".", true);
			sendCallForProposalEndMessage(ContractNetConstants.SECONDS_FOR_RETRY);
			sendCallForProposal(ContractNetConstants.SECONDS_FOR_RETRY);
		}
	}

	private void handleFailure() {
		LoggerUtil.log("The contractnet process has finished unsuccessfully.", true);
		done = true;
	}
	
	private void handleAcceptance() {
		LoggerUtil.log("The contract net process has finished.\nAccepting proposal from agent " + bestProposalAgent.getStr() + " of value " + bestProposal + ".", true);
		
		List<AID> runningAgents = ObjectFactory.getAgentManager().getRunningAgents();
		for(AID agent : runningAgents) {
			if(agent.getAgClass().toString().contains("ContractNetParticipant")) {
				if(agent.getStr().equals(bestProposalAgent.getStr())) {
					ACLMessage accept = new ACLMessage(Performative.ACCEPT_PROPOSAL);
					accept.receivers.add(bestProposalAgent);
					accept.sender = myAid;
					msm().post(accept);
				} else {
					ACLMessage reject = new ACLMessage(Performative.REJECT_PROPOSAL);
					reject.receivers.add(agent);
					reject.sender = myAid;
					msm().post(reject);
				}
			}
		}
	}
	
	private void removeParticipant(AID sender) {
		for(int i = 0; i < participants.size(); i++) {
			if(participants.get(i).getStr().equals(sender.getStr())) {
				participants.remove(i);
				break;
			}
		}
		if(participants.size() == 0) {
			finished(null);
		}
	}
	
	private void sendCallForProposalEndMessage(long replyBySeconds) {
		ACLMessage endMessage = new ACLMessage(Performative.INFORM);
		endMessage.sender = myAid;
		endMessage.receivers.add(myAid);
		msm().post(endMessage, replyBySeconds * 1000);
	}
	
	private void sendCallForProposal(long replyBySeconds) {
		ACLMessage callForProposal = new ACLMessage(Performative.CALL_FOR_PROPOSAL);
		callForProposal.receivers.addAll(participants);
		callForProposal.sender = myAid;
		callForProposal.replyBy = System.currentTimeMillis() + replyBySeconds * 1000;
		msm().post(callForProposal);
	}
}