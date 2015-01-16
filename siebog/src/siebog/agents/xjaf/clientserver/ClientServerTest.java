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

package siebog.agents.xjaf.clientserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.rmi.RemoteException;
import org.junit.Before;
import org.junit.Test;
import siebog.agents.test.TestClientBase;
import siebog.core.Global;
import siebog.utils.ObjectFactory;
import siebog.xjaf.agentmanager.AgentManager;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;
import siebog.xjaf.radigostlayer.RadigostStub;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class ClientServerTest extends TestClientBase {
	private AID clientAid;

	public ClientServerTest() throws RemoteException {
	}

	@Before
	public void before() {
		logger.info("For these tests to succeed, the ClientServer Radigost example should be running.");
		clientAid = new AID("CSClient", "ClientServer", RadigostStub.AGENT_CLASS);
	}

	@Test
	public void testClientServerMessaging() throws InterruptedException {
		// server-side agent
		AgentClass agClass = new AgentClass(Global.SIEBOG_MODULE, ClientServerAgent.class.getSimpleName());
		AgentManager agm = ObjectFactory.getAgentManager();
		AID csAgent = agm.startAgent(agClass, "CSServer" + System.currentTimeMillis(), null);

		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.sender = testAgentAid;
		msg.receivers.add(csAgent);
		msg.contentObj = clientAid;
		ObjectFactory.getMessageManager().post(msg);

		ACLMessage reply = pollMessage();

		assertNotNull(reply);
		assertEquals(Performative.INFORM, reply.performative);
	}

	@Test
	public void testStubs() {
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.sender = testAgentAid;
		msg.receivers.add(clientAid);
		msg.replyWith = "42";
		ObjectFactory.getMessageManager().post(msg);

		ACLMessage reply = pollMessage();

		assertNotNull(reply);
		assertEquals(msg.replyWith, reply.inReplyTo);
	}
}
