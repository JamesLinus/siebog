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

import siebog.SiebogClient;
import siebog.agents.AID;
import siebog.agents.Agent;
import siebog.agents.AgentClass;
import siebog.agents.AgentManager;
import siebog.interaction.ACLMessage;
import siebog.interaction.MessageManager;
import siebog.interaction.Performative;
import siebog.utils.ObjectFactory;

/**
 * The purpose of this test is to demonstrate the high availability of JGroups and Infinispan clusters. 
 * In order to run this test, create a cluster of at least two nodes and run the test. After the first 
 * iteration of messages disable a node (e.g. close the console) and pay attention to the logs. 
 * Notice that the agents that were on the node that was shutdown have appeared on the other nodes. 
 * Agents (or rather the underlying EJBs) exist on the cluster, and not on a particular node.
 * @author <a href="nikola.luburic@uns.ac.rs">Nikola Luburic</a>
 */
public class ClusterTest {
	private int NUMBER_OF_SLAVES = 6;
	private int NUMBER_OF_MESSAGES = 3;

	private void runCluster() throws InterruptedException {
		//Change "localhost" to a set of strings which represent IP addresses, 
		//where the first string is the master and the rest are slaves.
		SiebogClient.connect("localhost");
		
		AgentManager agm = ObjectFactory.getAgentManager();

		AgentClass agClass = new AgentClass(Agent.SIEBOG_MODULE, ClusterMaster.class.getSimpleName());
		AID pingAid = agm.startServerAgent(agClass, "ClusterMaster", null);

		for(int i = 0; i < NUMBER_OF_SLAVES; i++) {
			agm.startServerAgent(new AgentClass(Agent.SIEBOG_MODULE, ClusterSlave.class.getSimpleName()), "ClusterSlave" + i, null);
		}

		MessageManager msm = ObjectFactory.getMessageManager();
		ACLMessage message = new ACLMessage(Performative.REQUEST);
		message.receivers.add(pingAid);
		for(int i = 0; i < NUMBER_OF_MESSAGES; i++) {
			for(int j = 0; j < NUMBER_OF_SLAVES; j++) {
				message.content = "ClusterSlave" + j;
				msm.post(message);
			}
			Thread.sleep(4000);
		}
	}

	public static void main(String[] args) {
		try {
			new ClusterTest().runCluster();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

}
