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

import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import siebog.agents.AID;
import siebog.agents.AgentBuilder;
import siebog.interaction.ACLMsgBuilder;
import siebog.interaction.Performative;
import siebog.test.framework.MessageBasedTest;
import siebog.test.framework.receivers.MsgPatternBuilder;
import siebog.utils.ObjectFactory;

/**
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class SimplePingTest {
	private AID pingAgent;

	@Before
	public void setup() {
		// @formatter:off
		pingAgent = AgentBuilder.siebog()
			.ejb(SimplePingAgent.class)
			.randomName()
			.start();
		// @formatter:on
	}

	@After
	public void tearDown() {
		ObjectFactory.getAgentManager().stopAgent(pingAgent);
	}

	@Test
	public void testPingAgent() {
		// @formatter:off
		MessageBasedTest.which()
			.sends()
				.inSequence()
				.messages(ACLMsgBuilder
					.performative(Performative.REQUEST)
					.receivers(pingAgent))
				.and()
			.receives()
				.anyOf()
				.messages(MsgPatternBuilder
					.fromFields()
					.field("performative").equalTo(Performative.INFORM)
					.field("content").matches("Node name: \\p{Alnum}+"))
				.within(1, TimeUnit.SECONDS)
			.build()
			.execute();
		// @formatter:on
	}
}