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

package siebog.xjaf.managers;

import java.io.Serializable;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.Agent;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.fipa.ACLMessage;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class RunningAgent implements Serializable {
	private static final long serialVersionUID = 1L;
	private AgentClass agClass;
	private AID aid;
	// no getter for this one, since we don't want it serialized into JSON
	private Agent ref;

	public AgentClass getAgClass() {
		return agClass;
	}

	public void setAgClass(AgentClass agClass) {
		this.agClass = agClass;
	}

	public AID getAid() {
		return aid;
	}

	public void setAid(AID aid) {
		this.aid = aid;
	}

	public void setRef(Agent ref) {
		this.ref = ref;
	}

	public void handleMessage(ACLMessage msg) {
		try {
			ref.handleMessage(msg);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public void ping() {
		ref.ping();
	}
}
