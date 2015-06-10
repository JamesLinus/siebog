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
import siebog.agents.AgentInitArgs;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;
import dnars.base.Statement;
import dnars.events.EventObserver;
import dnars.events.EventPayload;
import dnars.graph.DNarsGraph;
import dnars.graph.DNarsGraphFactory;
import dnars.siebog.annotations.Domain;

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class DNarsAgent extends XjafAgent implements EventObserver {
	private static final long serialVersionUID = 1L;
	private transient DNarsGraph graph;
	private EventHandler eventHandler;

	@Override
	protected final void onInit(AgentInitArgs args) {
		doInit(args);
		eventHandler = new EventHandler(this);
		parseBeliefs();
	};

	@Override
	public void onEvents(EventPayload[] events) {
		ACLMessage acl = new ACLMessage(Performative.INFORM);
		acl.receivers.add(myAid);
		acl.contentObj = events;
		msm().post(acl);
	}

	@Override
	protected final void onMessage(ACLMessage msg) {
		if (msg.contentObj != null && msg.contentObj.getClass() == EventPayload[].class) {
			eventHandler.handle((EventPayload[]) msg.contentObj);
		} else {
			onAclMessage(msg);
		}
	}

	protected void onAclMessage(ACLMessage msg) {
	}

	protected void doInit(AgentInitArgs args) {
	}

	private void parseBeliefs() {
		BeliefParser bp = new BeliefParser(this);
		List<Statement> beliefs = bp.getInitialBeliefs();
		graph().include(beliefs.toArray(new Statement[0]));
	}

	protected DNarsGraph graph() {
		if (graph == null) {
			graph = createGraph();
		}
		return graph;
	}

	private DNarsGraph createGraph() {
		String domain = getDomain();
		DNarsGraph graph = DNarsGraphFactory.create(domain, null);
		graph.addObserver(this);
		return graph;
	}

	private String getDomain() {
		Domain domain = getClass().getAnnotation(Domain.class);
		return domain != null ? domain.name() : getClass().getName();
	}
}
