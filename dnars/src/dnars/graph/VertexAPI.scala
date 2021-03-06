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

package dnars.graph

import scala.collection.mutable.ListBuffer

import com.thinkaurelius.titan.core.Order
import com.thinkaurelius.titan.core.TitanVertex
import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Vertex

import dnars.base.Term
import dnars.graph.Wrappers.vertex2DNarsVertex

/**
 * Subset of the DNarsGraph API focused on vertices/terms.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
trait VertexAPI extends DNarsGraphAPI {
	override def getV(term: Term): Option[Vertex] = {
		val i = query().has("term", term.id).limit(1).vertices().iterator()
		if (i.hasNext())
			Some(i.next())
		else
			None
	}

	override def getOrAddV(term: Term): Vertex = {
		getV(term) match {
			case Some(v) => v
			case None =>
				val added = addV(null)
				added.term = term
				added
		}
	}

	override def getBestPredicates(subj: Term, copula: String, limit: Int): List[Term] = {
		getV(subj) match {
			case Some(v) =>
				val vertices = v.asInstanceOf[TitanVertex]
					.query()
					.labels(copula)
					.direction(Direction.OUT)
					.orderBy("predExp", Order.DESC)
					.limit(limit)
					.vertices()
				iterableToList(vertices)
			case None =>
				List()
		}
	}

	override def getBestSubjects(pred: Term, copula: String, limit: Int): List[Term] = {
		getV(pred) match {
			case Some(v) =>
				val vertices = v.asInstanceOf[TitanVertex]
					.query()
					.labels(copula)
					.direction(Direction.IN)
					.orderBy("subjExp", Order.DESC)
					.limit(limit)
					.vertices()
				iterableToList(vertices)
			case None =>
				List()
		}
	}

	private def iterableToList(vertices: java.lang.Iterable[Vertex]): List[Term] = {
		val iter = vertices.iterator()
		val res = ListBuffer[Term]()
		while (iter.hasNext())
			res += iter.next().term
		res.toList
	}
}