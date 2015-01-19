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

import siebog.dnars.graph.DNarsGraph
import siebog.dnars.base.Statement
import siebog.dnars.base.Copula._
import siebog.dnars.graph.DNarsEdge
import com.tinkerpop.blueprints.Edge
import siebog.dnars.graph.DNarsVertex
import com.tinkerpop.blueprints.Direction

/**
 * M ~ P, M -> S :: P -> S ana'
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class AnalogyInv(override val graph: DNarsGraph) extends ForwardInference(graph) {
	override def doApply(judgement: Statement): List[Statement] =
		if (judgement.copula != Inherit)
			List()
		else
			graph.getV(judgement.subj) match {
				case Some(m) =>
					val edges = m.outE(Similar).toList
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
			val ana = e.truth.analogy(judgement.truth, true)
			val derived = Statement(p, Inherit, judgement.pred, ana)
			keepIfValid(derived)
		}
	}

}