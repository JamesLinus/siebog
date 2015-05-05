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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import scala.collection.Iterator;
import siebog.agents.Agent;
import siebog.agents.XjafAgent;
import siebog.dnars.base.AtomicTerm;
import siebog.dnars.base.CompoundTerm;
import siebog.dnars.base.Copula;
import siebog.dnars.base.Statement;
import siebog.dnars.base.Truth;
import siebog.dnars.graph.DNarsGraph;
import siebog.dnars.graph.DNarsGraphFactory;
import siebog.dnars.inference.forward.ForwardInferenceEngine;
import siebog.interaction.ACLMessage;
import siebog.interaction.Performative;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class Learner extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private QueryDesc query;

	@Override
	protected void onMessage(ACLMessage msg) {
		if (msg.performative == Performative.REQUEST) {
			query = (QueryDesc) msg.contentObj;

			DNarsGraph allProps = DNarsGraphFactory.create(query.getAllProperties(), null);
			try {
				Set<Statement> relevant = getRelevantStatements(query.getQuestion(), allProps);
				logger.info("Retrieved " + relevant.size() + " relevant statements for "
						+ query.getQuestion());
				if (relevant.size() > 0) {

					ArrayList<Statement> intermediary = deriveNewConclusions(relevant);
					logger.info("Derived " + intermediary.size() + " itermediary statements for "
							+ query.getQuestion());
					if (intermediary.size() > 0) {

						Statement[] newKnowledge = null;
						DNarsGraph knownProps = DNarsGraphFactory.create(
								query.getKnownProperties(), null);
						try {
							newKnowledge = new ForwardInferenceEngine(knownProps)
									.conclusions(intermediary.toArray(new Statement[0]));
						} finally {
							knownProps.shutdown();
						}
						logger.info("Derived " + newKnowledge.length + " new statements for "
								+ query.getQuestion());

						for (Statement st : newKnowledge) {
							st = st.allImages().head();
							if (!allProps.hasAnswer(st)) {
								knownProps = DNarsGraphFactory.create(query.getKnownProperties(),
										null);
								try {
									knownProps.add(st);
								} finally {
									knownProps.shutdown();
								}
							}
						}
					}
				}
			} finally {
				allProps.shutdown();
			}

			ACLMessage reply = msg.makeReply(Performative.CONFIRM);
			msm().post(reply);

		}
	}

	@SuppressWarnings("unused")
	private Set<Statement> getRelevantStatements(String uri, DNarsGraph graph) {
		Set<Statement> set = new HashSet<>();
		AtomicTerm uriTerm = new AtomicTerm(uri);
		Truth truth = new Truth(1.0, 0.9);
		// uri -> ?
		Statement st = new Statement(uriTerm, Copula.Inherit(), AtomicTerm.Question(), truth);
		// Statement[] relevant = graph.answer(st, Integer.MAX_VALUE);
		// set.addAll(Arrays.asList(relevant));
		// ? -> uri
		st = new Statement(AtomicTerm.Question(), Copula.Inherit(), uriTerm, truth);
		// relevant = graph.answer(st, Integer.MAX_VALUE);
		// set.addAll(Arrays.asList(relevant));
		return set;
	}

	private ArrayList<Statement> deriveNewConclusions(Set<Statement> relevant) {
		ArrayList<Statement> conclusions = new ArrayList<>();
		DNarsGraph graph = DNarsGraphFactory.create(query.getKnownProperties(), null);
		try {
			Statement[] derived = new ForwardInferenceEngine(graph).conclusions(relevant
					.toArray(new Statement[0]));
			for (Statement d : derived) {
				if (d.subj() instanceof CompoundTerm && d.pred() instanceof CompoundTerm) {
					CompoundTerm subj = (CompoundTerm) d.subj();
					CompoundTerm pred = (CompoundTerm) d.pred();

					Iterator<AtomicTerm> si = subj.comps().iterator();
					Iterator<AtomicTerm> pi = pred.comps().iterator();
					while (si.hasNext() && pi.hasNext()) {
						AtomicTerm sterm = si.next();
						AtomicTerm pterm = pi.next();
						if (!sterm.equals(pterm))
							conclusions.add(new Statement(sterm, d.copula(), pterm, d.truth()));
					}
				} else
					conclusions.add(d);
			}
		} finally {
			graph.shutdown();
		}
		return conclusions;
	}
}
