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

package siebog.dnars;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import siebog.dnars.annotation.BeliefParser;
import siebog.dnars.events.EventPayload;
import siebog.dnars.graph.DNarsGraph;
import siebog.dnars.graph.DNarsGraphFactory;
import siebog.xjaf.agentmanager.AgentInitArgs;
import siebog.xjaf.core.XjafAgent;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class DNarsAgent extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private Map<String, DNarsGraph> domains;

	public DNarsAgent() {
		domains = new HashMap<>();
	}

	@PostConstruct
	public void postConstruct() {
		BeliefParser.getInitialBeliefs(this);
	}

	@Override
	protected void onInit(AgentInitArgs args) {
		super.onInit(args);
		String domainsStr = args.get("domains");
		if (domainsStr == null)
			domainsStr = myAid.getStr().replaceAll("[^a-zA-Z0-9_]", "_");
		final String[] domainsArray = domainsStr.split(",");
		for (String domainName : domainsArray)
			try {
				DNarsGraph graph = DNarsGraphFactory.create(domainName, null);
				graph.addObserver(myAid.toString());
				domains.put(domainName, graph);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
	}

	protected DNarsGraph domain(String domainName) {
		DNarsGraph graph = domains.get(domainName);
		if (graph == null)
			throw new IllegalArgumentException("No such domain: " + domainName);
		return graph;
	}

	protected Collection<DNarsGraph> domains() {
		return domains.values();
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

	protected void onEvents(EventPayload[] events) {
	}
}
