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

package siebog.agents.dnars.dbpedia;

import java.util.HashSet;
import java.util.Set;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import scala.collection.Iterator;
import siebog.agents.AID;
import siebog.agents.Agent;
import siebog.agents.AgentClass;
import siebog.agents.XjafAgent;
import siebog.core.Global;
import siebog.dnars.base.AtomicTerm;
import siebog.dnars.base.CompoundTerm;
import siebog.dnars.base.Connector;
import siebog.dnars.base.Statement;
import siebog.dnars.base.StatementParser;
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
public class Resolver extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private static final String properties = "props";

	@Override
	protected void onMessage(ACLMessage msg) {
		if (msg.performative == Performative.REQUEST) {
			// TODO : read from short abstracts
			ACLMessage reply = msg.makeReply(Performative.INFORM);
			reply.content = text;
			msm().post(reply);
			// store known properties and activate the annotator
			String knownProperties = storeKnownProperties(msg.content);
			QueryDesc query = new QueryDesc(msg.content, text, properties, knownProperties);
			activateAnnotator(query);
		}
	}

	private String storeKnownProperties(String query) {
		Set<Statement> known = getKnownProperties(query);
		int n = query.lastIndexOf('/');
		String domain = query.substring(n + 1).replaceAll("[^a-zA-Z]", "");
		DNarsGraph graph = DNarsGraphFactory.create(domain, null);
		try {
			for (Statement st : known)
				graph.add(st);
		} finally {
			graph.shutdown();
		}
		return domain;
	}

	@SuppressWarnings("unused")
	private Set<Statement> getKnownProperties(String query) {
		DNarsGraph graph = DNarsGraphFactory.create(properties, null);
		try {
			Statement question = StatementParser.apply(query + " -> ? (1.0, 0.9)");
			// Statement[] answers = graph.answer(question, Integer.MAX_VALUE);
			Set<Statement> known = new HashSet<>();
			// for (Statement st : answers) {
			// if (st.pred() instanceof AtomicTerm)
			// known.add(st);
			// else if (properExtensionalImage((CompoundTerm) st.pred()))
			// known.add(st);
			// }
			return known;
		} finally {
			graph.shutdown();
		}
	}

	// checks if the given term is (/ xxx * xxx)
	@SuppressWarnings("unused")
	private boolean properExtensionalImage(CompoundTerm term) {
		if (term.con().equals(Connector.ExtImage())) {
			Iterator<AtomicTerm> i = term.comps().iterator();
			i.next(); // xxx (relation)
			return i.next().equals(AtomicTerm.Placeholder());
		}
		return false;
	}

	private void activateAnnotator(QueryDesc query) {
		final String name = Annotator.class.getSimpleName();
		AgentClass cls = new AgentClass(Global.SIEBOG_MODULE, name);
		AID aid = agm().startAgent(cls, name + "_" + System.currentTimeMillis(), null);
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.receivers.add(aid);
		msg.contentObj = query;
		msm().post(msg);
	}

	private static final String text = "Albert Einstein (/\u02C8\u00E6lb\u0259rt \u02C8a\u026Ansta\u026An/; German: "
			+ "[\u02C8alb\u0250t \u02C8a\u026An\u0283ta\u026An] ; 14 March 1879 \u2013 18 April 1955) "
			+ "was a German-born theoretical physicist. He developed the general theory of relativity, "
			+ "one of the two pillars of modern physics (alongside quantum mechanics). He is best known "
			+ "for his mass\u2013energy equivalence formula E = mc2 (which has been dubbed \"the world's "
			+ "most famous equation\").";
}
