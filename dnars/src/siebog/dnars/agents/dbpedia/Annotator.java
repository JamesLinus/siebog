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

package siebog.dnars.agents.dbpedia;

import java.util.HashSet;
import java.util.Iterator;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;
import siebog.agents.AID;
import siebog.agents.Agent;
import siebog.agents.AgentClass;
import siebog.agents.AgentInitArgs;
import siebog.agents.XjafAgent;
import siebog.core.Global;
import siebog.dnars.base.Statement;
import siebog.dnars.graph.DNarsGraph;
import siebog.dnars.graph.DNarsGraphFactory;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class Annotator extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private static final String CONFIDENCE = "0.2";
	private static final String SUPPORT = "20";
	private QueryDesc query;
	private int pendingLearners;

	@Override
	protected void onMessage(ACLMessage msg) {
		if (msg.performative == Performative.REQUEST) {
			query = (QueryDesc) msg.contentObj;
			makeRequest();
		} else if (msg.performative == Performative.INFORM) { // annotated URIs received
			@SuppressWarnings("unchecked")
			HashSet<String> uris = (HashSet<String>) msg.contentObj;
			Iterator<String> i = uris.iterator();
			while (i.hasNext())
				if (i.next().equals(query.getQuestion()))
					i.remove();
			pendingLearners = uris.size();
			for (String u : uris)
				createNewLearner(u);
		} else if (msg.performative == Performative.CONFIRM) {
			--pendingLearners;
			logger.info("Pending learners: " + pendingLearners);
			if (pendingLearners == 0) {
				DNarsGraph graph = DNarsGraphFactory.create(query.getKnownProperties(), null);
				try {
					graph.forEachStatement(new AbstractFunction1<Statement, BoxedUnit>() {
						@Override
						public BoxedUnit apply(Statement st) {
							if (st.toString().contains(query.getQuestion()))
								if (st.truth().conf() > 0.7)
									System.out.println(st);
							return null;
						}
					});
				} finally {
					graph.shutdown();
				}
			}
		}
	}

	private void makeRequest() {
		Form form = new Form()// @formatter:off
			.param("text", query.getText())
			.param("confidence", CONFIDENCE)
			.param("support", SUPPORT);
		Entity<Form> entity = Entity.form(form);
		AnnotationProcessor p = new AnnotationProcessor(myAid);
		new ResteasyClientBuilder()
			.build()
			.target("http://spotlight.dbpedia.org/rest/annotate")
			.request()
			.accept(MediaType.APPLICATION_JSON)
			.async()
			.post(entity, p); // @formatter:on
	}

	private void createNewLearner(String uri) {
		final String name = Learner.class.getSimpleName();
		AgentClass agClass = new AgentClass(Global.SIEBOG_MODULE, name);
		AgentInitArgs args = new AgentInitArgs("query=" + query);
		AID aid = agm().startServerAgent(agClass, name + "_" + System.currentTimeMillis(), args);

		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.sender = myAid;
		msg.receivers.add(aid);
		msg.contentObj = new QueryDesc(uri, query.getText(), query.getAllProperties(), query.getKnownProperties());
		msm().post(msg);
	}
}
