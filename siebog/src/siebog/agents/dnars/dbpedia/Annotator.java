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

import java.io.Serializable;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Form;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import scala.collection.Iterator;
import siebog.core.Global;
import siebog.dnars.base.AtomicTerm;
import siebog.dnars.base.CompoundTerm;
import siebog.dnars.base.Connector;
import siebog.dnars.base.Statement;
import siebog.dnars.base.StatementParser;
import siebog.dnars.graph.DNarsGraph;
import siebog.dnars.graph.DNarsGraphFactory;
import siebog.dnars.inference.ResolutionEngine;
import siebog.xjaf.agentmanager.AgentInitArgs;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.Agent;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.core.XjafAgent;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;

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
	private RequestContent request;
	private Set<String> uris;
	private String tempDomain;

	public static class RequestContent implements Serializable {
		private static final long serialVersionUID = 1L;
		public String query;
		public String text;
		public String globalDomain;
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		if (msg.performative == Performative.REQUEST) {
			request = (RequestContent) msg.contentObj;
			makeRequest();
		}
	}

	private void makeRequest() {
		Form form = new Form()// @formatter:off
			.param("text", request.text)
			.param("confidence", CONFIDENCE)
			.param("support", SUPPORT);
		Entity<Form> entity = Entity.form(form);
		
		InvocationCallback<String> cb = new InvocationCallback<String>() {
			@Override
			public void completed(String response) {
				uris = getURIs(response);
				Set<Statement> relevant = getRelevantStatementsForQuery();
				tempDomain = "Annotator_" + System.currentTimeMillis();
				logger.info(String.format("Storing %d relevant statements for %s into %s", relevant.size(), request.query, tempDomain));
				storeRelevantStatements(relevant);
			}

			@Override
			public void failed(Throwable ex) {
				logger.log(Level.WARNING, "Error while annotating text.", ex);
			}
		};
		
		new ResteasyClientBuilder()
			.build()
			.target("http://spotlight.dbpedia.org/rest/annotate")
			.request()
			.async()
			.post(entity, cb); // @formatter:on
	}

	private Set<String> getURIs(String text) {
		Set<String> uris = new HashSet<>(); // there can be duplicates in the response
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(text)));
			NodeList nodeList = doc.getElementsByTagName("Resource");
			for (int i = 0, n = nodeList.getLength(); i < n; i++) {
				Node node = nodeList.item(i);
				uris.add(node.getAttributes().getNamedItem("URI").getNodeValue());
			}
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Error while parsing DBpedia Spotlight response.", ex);
		}
		return uris;
	}

	private Set<Statement> getRelevantStatementsForQuery() {
		DNarsGraph source = DNarsGraphFactory.create(request.globalDomain, null);
		try {
			Statement question = StatementParser.apply(request.query + " -> ? (1.0, 0.9)");
			Statement[] answers = ResolutionEngine.answer(source, question, Integer.MAX_VALUE);
			Set<Statement> relevant = new HashSet<>();
			for (Statement st : answers) {
				if (st.pred() instanceof AtomicTerm)
					relevant.add(st);
				else if (properExtensionalImage((CompoundTerm) st.pred()))
					relevant.add(st);
			}
			return relevant;
		} finally {
			source.shutdown();
		}
	}

	// checks if the given term is (/ xxx * xxx)
	private boolean properExtensionalImage(CompoundTerm term) {
		if (term.con().equals(Connector.ExtImage())) {
			Iterator<AtomicTerm> i = term.comps().iterator();
			i.next(); // xxx (relation)
			return i.next().equals(AtomicTerm.Placeholder());
		}
		return false;
	}

	void storeRelevantStatements(Set<Statement> relevant) {
		DNarsGraph graph = DNarsGraphFactory.create(tempDomain, null);
		try {
			for (Statement st : relevant)
				graph.statements().add(st);
		} finally {
			graph.shutdown();
		}
	}
}
