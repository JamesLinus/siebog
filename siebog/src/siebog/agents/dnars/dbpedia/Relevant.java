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
import siebog.dnars.DNarsAgent;
import siebog.dnars.base.AtomicTerm;
import siebog.dnars.base.Copula;
import siebog.dnars.base.Statement;
import siebog.dnars.base.Truth;
import siebog.dnars.graph.DNarsGraph;
import siebog.dnars.graph.StructuralTransform;
import siebog.dnars.inference.ResolutionEngine;
import siebog.xjaf.core.Agent;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;

/**
 * An agent that retrieves all relevant statements for the query. The response will include both the
 * relevant statement, and its extensional and intensional images (where applicable).
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class Relevant extends DNarsAgent {
	private static final long serialVersionUID = 1L;

	@Override
	protected void onMessage(ACLMessage msg) {
		if (msg.performative == Performative.REQUEST) {
			final String query = msg.content;
			DNarsGraph domain = domains().iterator().next();
			HashSet<Statement> relevant = getRelevantStatements(domain, query);

			ACLMessage reply = msg.makeReply(Performative.INFORM);
			reply.contentObj = relevant;
			msm().post(reply);
		}
	}

	private HashSet<Statement> getRelevantStatements(DNarsGraph domain, String query) {
		AtomicTerm queryTerm = new AtomicTerm(query);
		Truth truth = new Truth(1.0, 0.9);
		// query -> ?
		Statement st = new Statement(queryTerm, Copula.Inherit(), AtomicTerm.Question(), truth);
		Statement[] relevant1 = ResolutionEngine.answer(domain, st, Integer.MAX_VALUE);
		// ? -> query
		st = new Statement(AtomicTerm.Question(), Copula.Inherit(), queryTerm, truth);
		Statement[] relevant2 = ResolutionEngine.answer(domain, st, Integer.MAX_VALUE);
		// merge
		HashSet<Statement> set = new HashSet<>();
		addStatementsToSet(relevant1, set);
		addStatementsToSet(relevant2, set);
		return set;
	}

	private void addStatementsToSet(Statement[] statements, Set<Statement> set) {
		for (Statement st : statements) {
			set.add(st);
			// add the other two versions (if any)
			scala.collection.immutable.List<Statement> packed = StructuralTransform.pack(st);
			if (packed.size() == 2) {
				set.add(packed.head());
				set.add(packed.last());
			} else {
				scala.collection.immutable.List<Statement> unpacked = StructuralTransform.unpack(st);
				if (unpacked.size() == 2) {
					set.add(unpacked.head());
					set.add(unpacked.last());
				}
			}
		}
	}
}
