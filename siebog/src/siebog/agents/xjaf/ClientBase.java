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

package siebog.agents.xjaf;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import siebog.SiebogClient;
import siebog.core.Global;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.managers.AgentInitArgs;

/**
 * Base class for all client applications.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class ClientBase {
	protected Logger logger;
	protected BlockingQueue<ACLMessage> msgQueue;
	protected AID testAgentAid;

	public ClientBase() throws RemoteException {
		this("localhost");
	}

	public ClientBase(String masterAddr, String... slaveAddrs) throws RemoteException {
		SiebogClient.connect(masterAddr, slaveAddrs);
		logger = Logger.getLogger(getClass().getName());
		msgQueue = new LinkedBlockingQueue<>();
		startTestAgent(masterAddr);
	}

	private void startTestAgent(String masterAddr) throws RemoteException {
		Registry reg = LocateRegistry.createRegistry(1099);
		reg.rebind(RemoteAgentListener.class.getSimpleName(), new RemoteAgentListenerImpl(msgQueue));
		AgentClass agClass = new AgentClass(Global.SERVER, RemoteAgent.class.getSimpleName());
		AgentInitArgs args = new AgentInitArgs("remoteHost->" + masterAddr);
		testAgentAid = ObjectFactory.getAgentManager().startAgent(agClass, "testAgent", args);
	}
}
