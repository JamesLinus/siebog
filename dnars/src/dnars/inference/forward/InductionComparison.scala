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

package dnars.inference.forward

import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Edge

import dnars.base.Copula.Inherit
import dnars.base.Copula.Similar
import dnars.base.Statement
import dnars.graph.DNarsEdge
import dnars.graph.DNarsGraph
import dnars.graph.DNarsVertex
import dnars.graph.Wrappers.edge2DNarsEdge
import dnars.graph.Wrappers.vertex2DNarsVertex

/**
 * <pre><code>
 * M -> P, M -> S ::
 * 		S -> P ind,
 * 		S ~ P cmp
 * </code></pre>
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class InductionComparison(override val graph: DNarsGraph) extends ForwardInference(graph) {
	override def doApply(judgement: Statement): List[Statement] =
		if (judgement.copula != Inherit)
			List()
		else
			graph.getV(judgement.subj) match {
				case Some(m) =>
					val edges = m.outE(Inherit).toList
					edges.flatMap { e: Edge => inferForEdge(judgement, e) }
				case None =>
					List()
			}

	private def inferForEdge(judgement: Statement, e: DNarsEdge): List[Statement] = {
		val vertex: DNarsVertex = e.getVertex(Direction.IN)
		val p = vertex.term
		if (judgement.pred == p)
			List()
		else {
			val ind = e.truth.induction(judgement.truth)
			val cmp = e.truth.comparison(judgement.truth)
			val derivedInd = Statement(judgement.pred, Inherit, p, ind)
			val derivedCmp = Statement(judgement.pred, Similar, p, cmp)
			keepIfValid(derivedInd) ::: keepIfValid(derivedCmp)
		}
	}

}