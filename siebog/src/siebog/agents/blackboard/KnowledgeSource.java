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

package siebog.agents.blackboard;

import java.util.List;

import siebog.agentmanager.AID;
import siebog.agentmanager.XjafAgent;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.Performative;
import siebog.utils.ObjectFactory;

/**
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic<a>
 */

public abstract class KnowledgeSource extends XjafAgent {

	private static final long serialVersionUID = 1L;

	private List<String> trigers;
	private AID blackboardAID;
	
	public abstract void defineTriggers();
	
	public void defineTriggers(List<String> triggers,String blackboardName){
		this.trigers = triggers;
		blackboardAID = ObjectFactory.getAgentManager().getAIDByRuntimeName(blackboardName);
		for (String trigger: triggers){
			sendTrigger(trigger);
		}
	}
	
	public void sendTrigger(String trigger){
		ACLMessage msg = new ACLMessage(Performative.SUBSCRIBE);
		msg.receivers.add(blackboardAID);
		msg.content = trigger;
		msg.sender=myAid;
		msm().post(msg);
	}
	
	public void addTrigger(String trigger){
		trigers.add(trigger);
		sendTrigger(trigger);
	}
	
	public void handleInform(ACLMessage msg){
		Estimate e = giveEstimate((Event)msg.contentObj);
		e.setEvent((Event)msg.contentObj);
		ACLMessage proposal = new ACLMessage(Performative.PROPOSE);
		proposal.sender=myAid;
		proposal.receivers.add(blackboardAID);
		proposal.contentObj=e;
		proposal.content=((Event)msg.contentObj).getName();
		
		msm().post(proposal);
	}
	
	public abstract Estimate giveEstimate(Event e);
	
	public void handleAcceptProposal(ACLMessage msg){
		Event result = handleEvent(((Estimate)msg.contentObj).getEvent());
		ACLMessage myResult = new ACLMessage(Performative.INFORM);
		myResult.sender=myAid;
		myResult.receivers.add(blackboardAID);
		myResult.contentObj=result;
		myResult.content = "KS";
		msm().post(myResult);
	}
	
	public abstract Event handleEvent(Event e);
	
	@Override
	protected void onMessage(ACLMessage msg) {
		switch(msg.performative){
		case INFORM:
			handleInform(msg);
			break;
		case ACCEPT_PROPOSAL:
			handleAcceptProposal(msg);
			break;
		case REQUEST:
			defineTriggers();
			break;
		default:
			break;
		}
	}

}
