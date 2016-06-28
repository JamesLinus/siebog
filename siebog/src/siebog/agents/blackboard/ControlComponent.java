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
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.Performative;
import siebog.utils.ObjectFactory;

/**
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic<a>
 */

public abstract class ControlComponent {

	//method for evaluating proposals
	public abstract Estimate chooseBestProposal(List<Estimate> proposals);

	public void decide(List<Estimate> proposals){
		Estimate proposal = chooseBestProposal(proposals);
		ACLMessage msg  = new ACLMessage(Performative.ACCEPT_PROPOSAL);
		msg.receivers.add(proposal.getAid());
		ObjectFactory.getMessageManager().post(msg);
	}

	public void receiveEvent(Event e, AID bbAID){
		if (e.getName().equals("START")){
			ACLMessage msg = new ACLMessage(Performative.REQUEST);
			msg.receivers.addAll(e.getKSs());
			msg.content="CC";
			ObjectFactory.getMessageManager().post(msg);

		} 
		Event event = handleEvent(e);
		ACLMessage msg = new ACLMessage(Performative.INFORM);
		msg.contentObj = event;
		msg.receivers.add(bbAID);
		msg.content="CC";
		ObjectFactory.getMessageManager().post(msg);

	}

	protected abstract Event handleEvent(Event e);
}
