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

package siebog.dnars.inference.forward

import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Edge
import siebog.dnars.base.Copula.Inherit
import siebog.dnars.base.Copula.Similar
import siebog.dnars.base.Statement
import siebog.dnars.graph.DNarsEdge
import siebog.dnars.graph.DNarsGraph
import siebog.dnars.graph.Wrappers.edge2DNarsEdge
import siebog.dnars.graph.Wrappers.vertex2DNarsVertex
import siebog.dnars.base.Term

/**
 * <pre><code>
 * P -> M
 * 		S -> M	=> S -> P abd, S ~ P cmp
 * 		S ~ M 	=> P -> S ana
 * </code></pre>
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class AbductionComparisonAnalogy(override val graph: DNarsGraph) extends ForwardInference(graph) {
	override def doApply(judgement: Statement): List[Statement] =
		graph.getV(judgement.pred) match {
			case Some(m) =>
				val incomingEdges = m.inE(Inherit).toList
				incomingEdges.flatMap { e: Edge => inferForEdge(judgement, e) }
			case None =>
				List()
		}

	private def inferForEdge(judgement: Statement, e: Edge): List[Statement] = {
		val p = e.getVertex(Direction.OUT).term
		if (judgement.subj == p)
			List()
		else if (judgement.copula == Inherit)
			abduction(p, judgement, e) ::: comparison(p, judgement, e)
		else
			analogy(p, judgement, e)
	}

	private def abduction(p: Term, judgement: Statement, e: Edge): List[Statement] = {
		val truth = e.truth.abduction(judgement.truth)
		val derived = Statement(judgement.subj, Inherit, p, truth)
		keepIfValid(derived)
	}

	private def comparison(p: Term, judgement: Statement, e: Edge): List[Statement] = {
		val truth = e.truth.comparison(judgement.truth)
		val derived = Statement(judgement.subj, Similar, p, truth)
		keepIfValid(derived)
	}

	private def analogy(p: Term, judgement: Statement, e: Edge): List[Statement] = {
		val truth = e.truth.analogy(judgement.truth, false)
		val derived = Statement(p, Inherit, judgement.subj, truth)
		keepIfValid(derived)
	}
}