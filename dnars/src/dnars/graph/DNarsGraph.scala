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

import com.tinkerpop.gremlin.scala.ScalaGraph
import com.tinkerpop.blueprints.Graph
import dnars.base.Term
import com.tinkerpop.blueprints.Vertex
import dnars.base.Truth
import com.tinkerpop.blueprints.Edge
import dnars.base.Statement
import com.tinkerpop.gremlin.scala.ScalaVertex
import com.tinkerpop.blueprints.Direction
import dnars.base.CompoundTerm
import dnars.base.Connector._
import dnars.base.Copula._
import dnars.base.AtomicTerm

/**
 * Wrapper around the ScalaGraph class. Inspired by 
 * <a href="https://github.com/mpollmeier/gremlin-scala">gremlin/scala project</a>.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class DNarsGraph(override val graph: Graph) extends ScalaGraph(graph) {
	val statements = new StatementManager(this)
	
	def getV(term: Term): Option[Vertex] = {
		val vertex = v(term)
		if (vertex != null) Some(vertex) else None
	}

	def getOrAddV(term: Term): Vertex = {
		val existing = v(term)
		if (existing != null)
			existing
		else {
			val added = addV(term)
			DNarsVertex.wrap(added).term = term
			added
		}
	}
	
	def addE(subj: Vertex, copula: String, pred: Vertex, truth: Truth): Edge = {
		val edge = subj.addEdge(copula, pred)
		edge.setProperty("truth", truth)
		edge
	}
	
	def getE(st: Statement): Option[Edge] = {
		val s = v(st.subj)
		val p = v(st.pred)
		if (s == null || p == null) // no vertex, so no edge
			None
		else {
			// at least one vertex exists, check for an edge
			val subj: ScalaVertex = s
			val list = subj.outE(st.copula).as("x").inV.retain(Seq(p)).back("x").toList
			list match {
				case List() => None // nope
				case h :: Nil => Some(h.asInstanceOf[Edge])
				case _ => throw new IllegalStateException(s"Multiple edges of the same copula for $st")
			}
		}
	}
	
	def printEdges() {
		val list = E.map { e => e.toString + " " + e.getProperty("truth") }.toList
		for (e <- list)
			println(e)
	}
	
	def shutdown() = graph.shutdown()
}

object DNarsGraph {
	def apply(graph: ScalaGraph) = wrap(graph)
	implicit def wrap(graph: ScalaGraph) = new DNarsGraph(graph)
	implicit def unwrap(wrapper: DNarsGraph) = wrapper.graph
}