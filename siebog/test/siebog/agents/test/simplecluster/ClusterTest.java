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

import siebog.agentmanager.AID;
import siebog.agentmanager.Agent;
import siebog.agentmanager.AgentClass;
import siebog.agents.test.TestClientBase;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.Performative;

/**
 * The purpose of this test is to demonstrate the high availability of JGroups and Infinispan clusters. 
 * In order to run this test, create a cluster of at least two nodes and run the test. After the first 
 * iteration of messages disable a node (e.g. close the console) and pay attention to the logs. 
 * Notice that the agents that were on the node that was shutdown have appeared on the other nodes. 
 * Agents (or rather the underlying EJBs) exist on the cluster, and not on a particular node.
 * @author <a href="nikola.luburic@uns.ac.rs">Nikola Luburic</a>
 */
public class ClusterTest extends TestClientBase {
	private int NUMBER_OF_SLAVES = 6;
	private int NUMBER_OF_MESSAGES = 3;

	public void test() {
		AID clusterMasterAid = agm.startServerAgent(new AgentClass(Agent.SIEBOG_MODULE, ClusterMaster.class.getSimpleName()), "ClusterMaster", null);

		for(int i = 0; i < NUMBER_OF_SLAVES; i++) {
			agm.startServerAgent(new AgentClass(Agent.SIEBOG_MODULE, ClusterSlave.class.getSimpleName()), "ClusterSlave" + i, null);
		}
		
		ACLMessage message = new ACLMessage(Performative.REQUEST);
		message.receivers.add(clusterMasterAid);
		message.sender = testAgentAid;
		for(int i = 0; i < NUMBER_OF_MESSAGES; i++) {
			for(int j = 0; j < NUMBER_OF_SLAVES; j++) {
				message.content = "ClusterSlave" + j;
				msm.post(message);
			}
			//Alternatively:
			//message.content = "";
			//for(int j = 0; j < NUMBER_OF_SLAVES - 1; j++) {
			//	message.content += "ClusterSlave" + j + ",";
			//}
			//message.content += "ClusterSlave" + (NUMBER_OF_SLAVES - 1);
			//msm.post(message);
			//End alternative
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new ClusterTest().test();
	}
}
