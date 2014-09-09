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

package siebog.jasonee;

import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.mas2j.AgentParameters;
import java.util.Deque;
import java.util.LinkedList;
import siebog.xjaf.fipa.ACLMessage;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class SiebogAgArch extends AgArch {
	private Deque<ACLMessage> mailbox = new LinkedList<>();

	public void init(AgentParameters agp) throws Exception {
		Agent.create(this, agp.agClass.getClassName(), agp.getBBClass(), agp.asSource.getAbsolutePath(),
				agp.getAsSetts(false, false));
		insertAgArch(this);
		createCustomArchs(agp.getAgArchClasses());
	}

	public void reasoningCycle() {
		getTS().reasoningCycle();
	}

	public void onMessage(ACLMessage msg) {
		mailbox.add(msg);
	}
}
