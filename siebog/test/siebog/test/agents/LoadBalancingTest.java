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

package siebog.test.agents;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import siebog.agents.AID;
import siebog.agents.AgentBuilder;
import siebog.agents.test.loadbalancing.LoadBalanced;
import siebog.interaction.ACLMsgBuilder;
import siebog.interaction.Performative;
import siebog.test.framework.MessageBasedTest;
import siebog.test.framework.receivers.MsgPatternBuilder;

/**
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class LoadBalancingTest {
	@org.junit.Test
	public void testLoadBalancing() {
		// @formatter:off
		Set<AID> agentAids = new HashSet<>();
		agentAids.addAll(AgentBuilder
			.siebog()
			.js("LoadBalancing.js")
			.startNInstances(8));
		agentAids.addAll(AgentBuilder
			.siebog()
			.ejb(LoadBalanced.class)
			.startNInstances(8));
		
		MessageBasedTest.which()
			.sends()
				.inSequence()
				.messages(ACLMsgBuilder.performative(Performative.REQUEST)
							.receivers(agentAids))
				.and()
			.receives()
				.allInNoOrder()
				.messages(	MsgPatternBuilder
								.fromFields()
								.field("content").equalTo("server-master"),
							MsgPatternBuilder
								.fromFields()
								.field("content").equalTo("server-slave"),
							MsgPatternBuilder
								.fromFields()
								.field("content").equalTo("client-dev1"),
							MsgPatternBuilder
								.fromFields()
								.field("content").equalTo("client-dev2"))
			.build()
			.execute();
		// @formatter:on
	}
}
