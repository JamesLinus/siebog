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

package siebog.test.framework;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.agents.Agent;
import siebog.agents.AgentInitArgs;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;

/**
 * All messages sent to this agent will be forwarded to the remote RMI service.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class RemoteAgent extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(RemoteAgent.class);
	private static String remoteHost;

	@Override
	protected void onInit(AgentInitArgs args) {
		remoteHost = args.get("remoteHost");
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		try {
			getListener().onMessage(msg);
		} catch (RemoteException ex) {
			LOG.warn("Message forwarding failed.", ex);
		}
	}

	private RemoteListener getListener() {
		try {
			Registry reg = LocateRegistry.getRegistry(remoteHost);
			return (RemoteListener) reg.lookup(RemoteListener.class.getSimpleName());
		} catch (Exception ex) {
			throw new IllegalArgumentException("Cannot connect to the remote RMI service at "
					+ remoteHost + ".", ex);
		}
	}
}
