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

package siebog.agents.test.connection;

import java.rmi.RemoteException;

import org.junit.Test;

import siebog.agents.test.TestClientBase;
import siebog.core.Global;
import siebog.utils.ObjectFactory;
import siebog.xjaf.connectionmanager.TransferedAgent;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;

/**
 * 
 * @author <a href="l.arnold@live.com">Arnold Lacko</a>
 */
public class AgentTransferTest extends TestClientBase {

	/**
	 * @throws RemoteException
	 */
	public AgentTransferTest() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@Test
	public void testAgentTransfer() {
		AID aid = createAgent();
		ObjectFactory.getMessageManager().post(getMessage(aid));
	}
	
	/**
	 * @return
	 */
	private ACLMessage getMessage(AID aid) {
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.receivers.add(aid);
		return msg;
	}

	private AID createAgent() {
		AgentClass cls = new AgentClass(Global.SIEBOG_MODULE, TransferedAgent.class.getSimpleName());
		return agm.startAgent(cls, "TA" + System.currentTimeMillis(), null);
	}

}
