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

import com.tinkerpop.blueprints.Edge
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.Vertex
import dnars.base.Truth
import dnars.base.Statement
import dnars.graph.Wrappers._
import com.tinkerpop.gremlin.scala.ScalaVertex

/**
 * Subset of the DNarsGraph API focused on raw graph edges.
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
trait EdgeAPI extends DNarsGraphAPI {
	override def addE(subj: Vertex, copula: String, pred: Vertex, truth: Truth): Edge = {
		val edge = subj.addEdge(copula, pred)
		edge.truth = truth
		edge
	}

	override def getE(st: Statement): Option[Edge] = {
		val subjOpt = getV(st.subj)
		val predOpt = getV(st.pred)
		(subjOpt, predOpt) match {
			case (Some(subj), Some(pred)) =>
				val list = subj.outE(st.copula).as("x").inV.retain(Seq(pred)).back("x").toList
				list match {
					case List() => None // nope
					case h :: Nil => Some(h.asInstanceOf[Edge])
					case _ => throw new IllegalStateException(s"Multiple edges of the same copula for $st")
				}
			case _ => // no vertex, so no edge
				None
		}
	}
}