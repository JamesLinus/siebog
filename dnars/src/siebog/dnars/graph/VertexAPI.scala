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

package siebog.dnars.graph

import scala.collection.mutable.ListBuffer

import com.thinkaurelius.titan.core.Order
import com.thinkaurelius.titan.core.TitanVertex
import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Vertex

import siebog.dnars.base.Term
import siebog.dnars.graph.Wrappers.vertex2DNarsVertex

/**
 * Subset of the DNarsGraph API focused on vertices/terms.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
trait VertexAPI extends DNarsGraphAPI {
	override def getV(term: Term): Option[Vertex] = {
		//val i = query().has("term", term.id).limit(1).vertices().iterator()
		//		if (i.hasNext())
		//			Some(i.next())
		//		else
		//			None
		try {
			Some(V.has("term", term.id).next())
		} catch {
			case _: NoSuchElementException => None
		}
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
				val i = v.asInstanceOf[TitanVertex].query().labels(copula).direction(Direction.OUT).orderBy("predExp", Order.DESC).limit(limit).vertices().iterator()
				val res = ListBuffer[Term]()
				while (i.hasNext())
					res += i.next().term
				res.toList
			case None =>
				List()
		}
	}

	override def getBestSubjects(pred: Term, copula: String, limit: Int): List[Term] = {
		getV(pred) match {
			case Some(v) =>
				val i = v.asInstanceOf[TitanVertex].query().labels(copula).direction(Direction.IN).orderBy("subjExp", Order.DESC).limit(limit).vertices().iterator()
				val res = ListBuffer[Term]()
				while (i.hasNext())
					res += i.next().term
				res.toList
			case None =>
				List()
		}
	}
}