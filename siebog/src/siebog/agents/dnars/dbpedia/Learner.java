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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import scala.collection.Iterator;
import siebog.dnars.base.AtomicTerm;
import siebog.dnars.base.CompoundTerm;
import siebog.dnars.base.Copula;
import siebog.dnars.base.Statement;
import siebog.dnars.base.Truth;
import siebog.dnars.graph.DNarsGraph;
import siebog.dnars.graph.DNarsGraphFactory;
import siebog.dnars.inference.ForwardInference;
import siebog.dnars.inference.ResolutionEngine;
import siebog.xjaf.core.Agent;
import siebog.xjaf.core.XjafAgent;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;

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

			Set<Statement> relevant = getRelevantStatements(query.getQuestion());
			logger.info("Retrieved " + relevant.size() + " relevant statements for " + query.getQuestion());
			ArrayList<Statement> conclusions;
			if (relevant.size() > 0)
				conclusions = deriveNewConclusions(relevant);
			else
				conclusions = new ArrayList<>();

			ACLMessage reply = msg.makeReply(Performative.CONFIRM);
			reply.contentObj = conclusions;
			msm().post(reply);
		}
	}

	private Set<Statement> getRelevantStatements(String uri) {
		Set<Statement> set = new HashSet<>();
		DNarsGraph graph = DNarsGraphFactory.create(query.getAllProperties(), null);
		try {
			AtomicTerm uriTerm = new AtomicTerm(uri);
			Truth truth = new Truth(1.0, 0.9);
			// uri -> ?
			Statement st = new Statement(uriTerm, Copula.Inherit(), AtomicTerm.Question(), truth);
			Statement[] relevant1 = ResolutionEngine.answer(graph, st, Integer.MAX_VALUE);
			// ? -> uri
			st = new Statement(AtomicTerm.Question(), Copula.Inherit(), uriTerm, truth);
			Statement[] relevant2 = ResolutionEngine.answer(graph, st, Integer.MAX_VALUE);
			addStatementsToSet(relevant1, set);
			addStatementsToSet(relevant2, set);
		} finally {
			graph.shutdown();
		}
		return set;
	}

	private void addStatementsToSet(Statement[] statements, Set<Statement> set) {
		for (Statement st : statements) {
			set.add(st);
			// TODO add this as a feature of ForwardInference
			// add the other two versions (if any)
			scala.collection.immutable.List<Statement> packed = st.pack();
			if (packed.size() == 2) {
				set.add(packed.head());
				set.add(packed.last());
			} else {
				scala.collection.immutable.List<Statement> unpacked = st.unpack();
				if (unpacked.size() == 2) {
					set.add(unpacked.head());
					set.add(unpacked.last());
				}
			}
		}
	}

	private ArrayList<Statement> deriveNewConclusions(Set<Statement> relevant) {
		ArrayList<Statement> conclusions = new ArrayList<>();
		DNarsGraph graph = DNarsGraphFactory.create(query.getKnownProperties(), null);
		try {
			for (Statement st : relevant) {
				Statement[] derived = ForwardInference.conclusions(graph, st);
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
			}
		} finally {
			graph.shutdown();
		}
		return conclusions;
	}
}
