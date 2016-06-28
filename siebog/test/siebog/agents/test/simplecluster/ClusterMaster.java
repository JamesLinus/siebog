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

package siebog.agents.test.simplecluster;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import siebog.agentmanager.AID;
import siebog.agentmanager.Agent;
import siebog.agentmanager.AgentClass;
import siebog.agentmanager.XjafAgent;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.Performative;
import siebog.utils.LoggerUtil;

/**
 * Example of a cluster master agent. The agent accepts messages, logs them and, in case of a REQUEST, 
 * examines the 'content' which contains the name of a ClusterSlave agent. The ClusterMaster 
 * forwards the message to that slave and waits for a reply from the slave (INFORM performative).
 *
 * @author <a href="nikola.luburic@uns.ac.rs">Nikola Luburic</a>
 */
@Stateful
@Remote(Agent.class)
public class ClusterMaster extends XjafAgent {
	private static final long serialVersionUID = 1L;

	@Override
	protected void onMessage(ACLMessage msg) {
		LoggerUtil.logMessage(msg, myAid);
		if (msg.performative == Performative.REQUEST) {
			AgentClass agClass = new AgentClass(Agent.SIEBOG_MODULE, ClusterSlave.class.getSimpleName());
			String[] slaves = msg.content.split(",");
			
			ACLMessage msgToSlave = new ACLMessage(Performative.REQUEST);
			msgToSlave.sender = myAid;
			for(String slave : slaves) {
				AID slaveAid = new AID(slave, agClass);
				msgToSlave.receivers.add(slaveAid);
			}
			msm().post(msgToSlave);
		}
	}
}