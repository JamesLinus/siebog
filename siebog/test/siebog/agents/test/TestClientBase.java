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

package siebog.agents.test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import siebog.SiebogClient;
import siebog.agentmanager.AID;
import siebog.agentmanager.AgentManager;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.MessageManager;
import siebog.utils.ObjectFactory;

/**
 * Base class for all test client applications.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 * @author <a href="nikola.luburic@uns.ac.rs">Nikola Luburic</a>
 */
public abstract class TestClientBase {
	protected Logger logger;
	protected BlockingQueue<ACLMessage> msgQueue;
	protected AID testAgentAid;
	protected AgentManager agm;
	protected MessageManager msm;

	public TestClientBase() {
		TestProps props = TestProps.get();
		SiebogClient.connect(props.getMaster(), props.getSlaves());
		logger = Logger.getLogger(getClass().getName());
		msgQueue = new LinkedBlockingQueue<>();
		testAgentAid = new AID("testAgent", "testAgent", null);
		agm = ObjectFactory.getAgentManager();
		msm = ObjectFactory.getMessageManager();
	}
	
	public abstract void test();

	protected ACLMessage pollMessage() {
		try {
			return msgQueue.poll(5, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			return null;
		}
	}
}
