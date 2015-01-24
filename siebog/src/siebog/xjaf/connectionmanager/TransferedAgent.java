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

package siebog.xjaf.connectionmanager;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

import org.jgroups.Address;

import siebog.xjaf.core.Agent;
import siebog.xjaf.core.XjafAgent;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;

/**
 * 
 * @author <a href="l.arnold@live.com">Arnold Lacko</a>
 */
@Stateful
@Remote(Agent.class)
public class TransferedAgent extends XjafAgent {

	@EJB
	ConnectionManager connectionManager;
	
    @Resource
    SessionContext sc;
	
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see siebog.xjaf.core.XjafAgent#onMessage(siebog.xjaf.fipa.ACLMessage)
	 */
	@Override
	protected void onMessage(ACLMessage msg) {
		if (msg.performative == Performative.REQUEST) {
			System.out.println("TRANSFERING " + this);
			transfer();
		}
	}

	/**
	 * 
	 */
	private void transfer() {
		Address local = connectionManager.getLocalAddress();
		List<Address> members = connectionManager.getMembers();
		if (members.size() < 2) {
			System.out.println("NO PLACE TO TRANSFER TO");
		}
		else {
			try {
				if (members.get(0).equals(local)) {
					connectionManager.send(members.get(1), this);
				}
				else {
					connectionManager.send(members.get(0), this);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
