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

package siebog.agents.xjaf.ping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import siebog.agents.xjaf.ClientBase;
import siebog.core.Global;
import siebog.utils.ObjectFactory;
import siebog.xjaf.agentmanager.AgentManager;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;
import siebog.xjaf.messagemanager.MessageManager;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class PingPongTest extends ClientBase {
	public PingPongTest() throws RemoteException {
	}

	@Test
	public void testPingPong() throws InterruptedException {
		runPingPong();
		ACLMessage reply = msgQueue.poll(10, TimeUnit.SECONDS);
		assertNotNull(reply);
		assertEquals(Performative.INFORM, reply.performative);
	}

	private void runPingPong() {
		AgentManager agm = ObjectFactory.getAgentManager();

		AID pingAid = agm.startAgent(new AgentClass(Global.SERVER, "Ping"), "Ping", null);

		String pongName = "Pong";
		agm.startAgent(new AgentClass(Global.SERVER, "Pong"), pongName, null);

		MessageManager msm = ObjectFactory.getMessageManager();
		ACLMessage message = new ACLMessage(Performative.REQUEST);
		message.receivers.add(pingAid);
		message.content = pongName;
		message.replyTo = testAgentAid;
		msm.post(message);
	}

	public static void main(String[] args) {
		try {
			new PingPongTest().testPingPong();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

}
