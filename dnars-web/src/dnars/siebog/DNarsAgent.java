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

package dnars.siebog;

import java.util.List;
import javax.annotation.PostConstruct;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import dnars.base.Statement;
import dnars.graph.DNarsGraph;
import dnars.graph.DNarsGraphFactory;
import dnars.siebog.annotations.BeliefParser;
import dnars.siebog.annotations.Domain;

//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//import javax.annotation.PostConstruct;
//import dnars.annotation.BeliefParser;
//import dnars.events.EventPayload;
//import dnars.graph.DNarsGraph;
//import dnars.graph.DNarsGraphFactory;
//import siebog.xjaf.agentmanager.AgentInitArgs;
//import siebog.xjaf.core.XjafAgent;
//import siebog.xjaf.fipa.ACLMessage;
//import siebog.xjaf.fipa.Performative;

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class DNarsAgent extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private DNarsGraph graph;

	// private Map<String, DNarsGraph> domains;
	//
	// public DNarsAgent() {
	// domains = new HashMap<>();
	// }
	//
	@PostConstruct
	public void postConstruct() {
		graph = createGraph();
		parseBeliefs();
	}

	private DNarsGraph createGraph() {
		String domain = getDomain();
		return DNarsGraphFactory.create(domain, null);
	}

	private String getDomain() {
		Domain domain = getClass().getAnnotation(Domain.class);
		return domain != null ? domain.name() : getClass().getName();
	}

	private void parseBeliefs() {
		BeliefParser bp = new BeliefParser(this);
		List<Statement> beliefs = bp.getInitialBeliefs();
		graph.include(beliefs.toArray(new Statement[0]));
	}

	@Override
	protected void onMessage(ACLMessage msg) {
	}

	//
	// @Override
	// protected void onInit(AgentInitArgs args) {
	// super.onInit(args);
	// String domainsStr = args.get("domains");
	// if (domainsStr == null)
	// domainsStr = myAid.getStr().replaceAll("[^a-zA-Z0-9_]", "_");
	// final String[] domainsArray = domainsStr.split(",");
	// for (String domainName : domainsArray)
	// try {
	// DNarsGraph graph = DNarsGraphFactory.create(domainName, null);
	// graph.addObserver(myAid.toString());
	// domains.put(domainName, graph);
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// }
	//
	// protected DNarsGraph domain(String domainName) {
	// DNarsGraph graph = domains.get(domainName);
	// if (graph == null)
	// throw new IllegalArgumentException("No such domain: " + domainName);
	// return graph;
	// }
	//
	// protected Collection<DNarsGraph> domains() {
	// return domains.values();
	// }
	//
	// protected boolean filter(ACLMessage msg) {
	// if (msg.performative == Performative.INFORM) {
	// // TODO : String to Event[]
	// // Event[] events = (Event[]) msg.getContent();
	// onEvents(null);
	// return false;
	// }
	// return true;
	// }
	//
	// protected void onEvents(EventPayload[] events) {
	// }
}
