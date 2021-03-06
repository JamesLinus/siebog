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

package siebog.agents.test.loadbalancing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import siebog.agents.AID;
import siebog.agents.Agent;
import siebog.agents.AgentClass;
import siebog.agents.test.TestClientBase;
import siebog.agents.test.TestProps;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;

/**
 * Tests if agents are properly distributed over all cluster nodes.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class LoadBalancingTest extends TestClientBase {
	private static final int NUM_AGENTS_TO_CREATE = 32;
	private Set<String> unusedNodes;

	public LoadBalancingTest() throws RemoteException {
		String[] slaves = TestProps.get().getSlaves();
		unusedNodes = new HashSet<>(slaves.length + 1);
		unusedNodes.add("master:xjaf-master");
		for (String s : slaves)
			unusedNodes.add("slave:" + s);
	}

	@Test
	public void testLoadBalancing() {
		for (int i = 0; i < NUM_AGENTS_TO_CREATE && unusedNodes.size() > 0; i++) {
			AID aid = createAgent();
			ACLMessage response = getResult(aid);
			assertNotNull(response);
			unusedNodes.remove(response.content);
		}
		assertEquals("Unused nodes: " + unusedNodes, 0, unusedNodes.size());
	}

	private AID createAgent() {
		AgentClass cls = new AgentClass(Agent.SIEBOG_MODULE, LoadBalanced.class.getSimpleName());
		return agm.startServerAgent(cls, "LB" + System.currentTimeMillis(), null);
	}

	private ACLMessage getResult(AID aid) {
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.receivers.add(aid);
		msg.replyTo = testAgentAid;
		msm.post(msg);
		return pollMessage();
	}
}
