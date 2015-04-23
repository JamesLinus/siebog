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

package siebog.interaction.blackboard;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import siebog.agents.AID;

import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import siebog.interaction.contractnet.Delay;
import siebog.utils.ObjectFactory;

/**
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic<a>
 */

public abstract class Blackboard extends XjafAgent {

	//different ks need to be informed when different events occur
	//hash map - key is the simple name of the event that triggered it,
	//and the value is the collection of AIDs of the KSs that need to be informed

	private static final long serialVersionUID = 1L;
	private HashMap<String,List<AID>> notifications = new HashMap<>();
	private List<Event> events = new ArrayList<>();
	private HashMap<String,List<Estimate>> estimates = new HashMap<>();

	public void startBlackboard(Event event){
		events.add(event);
		notifications.put(event.getName(), ObjectFactory.getAgentManager().getRunningAgents());
		newEventNotification(event);
	}

	public void addTriggers(ACLMessage msg){
		if (notifications.get(msg.content)==null ){
			List<AID> list = new ArrayList<>();
			list.add(msg.sender);
			notifications.put(msg.content, list);
		} else {
			notifications.get(msg.content).add(msg.sender);
		}
	}

	public void newEventNotification(Event event){
		ACLMessage msg = new ACLMessage(Performative.INFORM);
		msg.sender=myAid;

		msg.contentObj = event;

		msg.receivers = notifications.get(event.getName());

	
		if (msg.receivers!=null){
			msm().post(msg);
		}

		//send delayed message
		ACLMessage delayedMsg = new ACLMessage();
		delayedMsg.sender=myAid;
		delayedMsg.receivers.add(myAid);
		//wait for 2 seconds for KS to send their estimation
		Delay delay = new Delay(System.currentTimeMillis()+2000);
		delayedMsg.contentObj = delay;
		delayedMsg.content = event.getName();
		msm().post(delayedMsg);
	}

	public void handleProposal(ACLMessage msg){
		Estimate e = (Estimate) msg.contentObj;
		String eventName = e.getEvent().getName();
		if (estimates.get(eventName)==null){
			List<Estimate> list = new ArrayList<>();
			list.add(e);
			estimates.put(eventName, list);
		} else {
			estimates.get(eventName).add(e);
		}

	}

	public abstract ControlComponent getControlComponent();

	public void acceptProposal(Estimate e){
		ACLMessage msg = new ACLMessage(Performative.ACCEPT_PROPOSAL);
		msg.sender=myAid;
		msg.receivers.add(e.getAid());
		msg.contentObj=e;
		msm().post(msg);
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		switch(msg.performative){
		case SUBSCRIBE:
			addTriggers(msg);
			break;
		case INFORM:
			if (msg.content.equals("CC"))
				newEventNotification((Event)msg.contentObj);
			else
				getControlComponent().receiveEvent((Event) msg.contentObj, myAid);
			break;
		case PROPOSE:
			handleProposal(msg);
			break;
		default:

			if (estimates.get(msg.content)!=null) {
				if (estimates.get(msg.content).size()>1){
					Estimate bestEstimate = getControlComponent().chooseBestProposal(estimates.get(msg.content));
					acceptProposal(bestEstimate);
				} else {
					acceptProposal(estimates.get(msg.content).get(0));

				} }
			break;
		}

	} 

}
