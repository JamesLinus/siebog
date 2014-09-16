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

package siebog.xjaf.dnarslayer;

import siebog.dnars.events.Event;
import siebog.dnars.graph.DNarsGraph;
import siebog.dnars.graph.DNarsGraphFactory;
import siebog.xjaf.core.XjafAgent;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;
import siebog.xjaf.managers.AgentInitArgs;

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class DNarsAgent extends XjafAgent {
	private static final long serialVersionUID = 1L;
	protected DNarsGraph graph;

	@Override
	protected void onInit(AgentInitArgs args) {
		super.onInit(args);
		String domain = args.get("domain");
		if (domain == null)
			domain = myAid.toString();
		try {
			graph = DNarsGraphFactory.create(domain, null);
			graph.addObserver(myAid.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected boolean filter(ACLMessage msg) {
		if (msg.performative == Performative.INFORM) {
			// TODO : String to Event[]
			// Event[] events = (Event[]) msg.getContent();
			onEvents(null);
			return false;
		}
		return true;
	}

	protected void onEvents(Event[] events) {
	}
}
