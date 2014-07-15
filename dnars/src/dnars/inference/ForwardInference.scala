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

package dnars.inference

import com.tinkerpop.gremlin.scala.ScalaGraph
import dnars.base.Statement
import dnars.base.Copula._
import com.tinkerpop.gremlin.scala.ScalaVertex
import com.tinkerpop.blueprints.Direction
import dnars.base.Truth
import dnars.base.Term
import dnars.gremlin.DNarsGraph
import dnars.gremlin.DNarsVertex
import dnars.gremlin.DNarsEdge

/**
 * Syllogistic forward inference rules. The following table represents the summary
 * of supported premises and conclusions (up to NAL-5).
 *
 *       | S->M                | S<->M     | M->S
 * ------|---------------------|-----------|--------------------
 * M->P  | S->P ded            | S->P ana  | S->P ind, S<->P cmp
 * -----------------------------------------------------------
 * M<->P | S->P ana'           | S<->P res | P->S ana'
 * -----------------------------------------------------------
 * P->M  | S->P abd, S<->P cmp | P->S ana  | --
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
object ForwardInference {
	def apply(graph: DNarsGraph, statements: Seq[Statement]): Unit = {
		for (st <- statements) {
			graph.statements.add(st)
			deduction_analogy(graph, st)
			analogy_resemblance(graph, st)
			abduction_comparison_analogy(graph, st)
			induction_comparison(graph, st)
			analogy_inv(graph, st)
		}
	}
	
	// in the following functions, the first premise is taken from the graph,
	// while the second premise is passed as a parameter
	
	// M -> P  
	//		S -> M	=> S -> P ded 
	//		S ~ M	=> S -> P ana
	def deduction_analogy(graph: DNarsGraph, st: Statement): Unit = {
		val m: ScalaVertex = graph.v(st.pred)
		val edges = m.outE(Inherit).toList
		for (e <- edges) {
			val vertex: DNarsVertex = e.getVertex(Direction.IN) 
			val p = vertex.term
			if (st.subj != p) {
				val edge: DNarsEdge = e
				val newTruth = 
					if (st.copula == Inherit) 
						edge.truth.deduction(st.truth) 
					else 
						edge.truth.analogy(st.truth, false)
				graph.statements.add(Statement(st.subj, Inherit, p, newTruth))
			}
		}
	}
	
	// M ~ P ::
	//		S -> M	=> S -> P ana'
	//		S ~ M	=> S ~ P res
	def analogy_resemblance(graph: DNarsGraph, st: Statement): Unit = {
		val m: ScalaVertex = graph.v(st.pred)
		val edges = m.outE(Similar).toList
		for (e <- edges) {
			val vertex: DNarsVertex = e.getVertex(Direction.IN)
			val p = vertex.term
			if (st.subj != p) {
				val edge: DNarsEdge = e
				if (st.copula == Inherit)
					graph.statements.add(Statement(st.subj, Inherit, p, edge.truth.analogy(st.truth, true)))
				else
					graph.statements.add(Statement(st.subj, Similar, p, edge.truth.resemblance(st.truth)))
			}
		}
	}	
	
	// P -> M 
	//		S -> M	=> S -> P abd, S ~ P cmp
	//		S ~ M 	=> P -> S ana
	def abduction_comparison_analogy(graph: DNarsGraph, st: Statement): Unit = {
		val m: ScalaVertex = graph.v(st.pred)
		val edges = m.inE(Inherit).toList
		for (e <- edges) {
			val vertex: DNarsVertex = e.getVertex(Direction.OUT)
			val p = vertex.term
			if (st.subj != p) {
				val edge: DNarsEdge = e
				if (st.copula == Inherit) {
					val abd = edge.truth.abduction(st.truth)
					graph.statements.add(Statement(st.subj, Inherit, p, abd))
					val cmp = edge.truth.comparison(st.truth)
					graph.statements.add(Statement(st.subj, Similar, p, cmp))
				}
				else {
					val ana = edge.truth.analogy(st.truth, false)
					graph.statements.add(Statement(p, Inherit, st.subj, ana))
				}
			}
		}
	}
	
	// M -> P, M -> S => S -> P ind, S ~ P cmp
	def induction_comparison(graph: DNarsGraph, st: Statement): Unit = {
		if (st.copula == Inherit) {
			val m: ScalaVertex = graph.v(st.subj)
			val edges = m.outE(Inherit).toList
			for (e <- edges) {
				val vertex: DNarsVertex = e.getVertex(Direction.IN)
				val p = vertex.term
				if (st.pred != p) {
					val edge: DNarsEdge = e
					val ind = edge.truth.induction(st.truth)
					graph.statements.add(Statement(st.pred, Inherit, p, ind))
					val cmp = edge.truth.comparison(st.truth)
					graph.statements.add(Statement(st.pred, Similar, p, cmp))
				}
			}
		}
	}
	
	// M ~ P, M -> S => P -> S ana'
	def analogy_inv(graph: DNarsGraph, st: Statement): Unit = {
		if (st.copula == Inherit) {
			val m: ScalaVertex = graph.v(st.subj)
			val edges = m.outE(Similar).toList
			for (e <- edges) {
				val vertex: DNarsVertex = e.getVertex(Direction.IN)
				val p = vertex.term
				if (st.pred != p) {
					val edge: DNarsEdge = e
					val ana = edge.truth.analogy(st.truth, true)
					graph.statements.add(Statement(p, Inherit, st.pred, ana))
				}
			}
		}
	}
}