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

import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Edge
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.gremlin.scala.ScalaEdge
import com.tinkerpop.gremlin.scala.ScalaVertex

import dnars.base.AtomicTerm
import dnars.base.CompoundTerm
import dnars.base.Term
import dnars.base.Truth

/**
 * Wrappers around Edge and Vertex classes. Inspired by the
 * <a href="https://github.com/mpollmeier/gremlin-scala">gremlin/scala</a> project.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */

class DNarsEdge(override val edge: Edge) extends ScalaEdge(edge) {
	def copula(): String = getLabel

	def truth: Truth = {
		val str = getProperty[String]("truth").split("_")
		val freq = str(0).toDouble
		val conf = str(1).toDouble
		Truth(freq, conf)
	}

	def truth_=(value: Truth): Unit = {
		import Wrappers.vertex2DNarsVertex
		val str = value.freq + "_" + value.conf
		setProperty("truth", str)
		//
		val s = getVertex(Direction.OUT)
		val sexp = value.expectation * (1.0 / s.term.complexity)
		setProperty("subjExp", (sexp * 1000).toInt)
		// 
		val p = getVertex(Direction.IN)
		val pexp = value.expectation * (1.0 / p.term.complexity)
		setProperty("predExp", (pexp * 1000).toInt)
	}
}

class DNarsVertex(override val vertex: Vertex) extends ScalaVertex(vertex) {
	def term: Term = {
		val id = getProperty[String]("term")
		if (id.charAt(0) == '(') {
			val elems = id.substring(1, id.length - 1).split(" ")
			val comps = for (c <- elems.tail)
				yield AtomicTerm(c)
			CompoundTerm(elems(0), comps.toList)
		} else
			AtomicTerm(id)
	}

	def term_=(value: Term): Unit =
		setProperty("term", value.id)
}

object Wrappers {
	implicit def edge2DNarsEdge(e: Edge): DNarsEdge = new DNarsEdge(e)
	implicit def vertex2DNarsVertex(v: Vertex): DNarsVertex = new DNarsVertex(v)
}