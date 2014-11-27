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

package siebog.dnars.inference

import scala.collection.mutable.ListBuffer
import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.gremlin.scala.ScalaVertex
import siebog.dnars.base.Copula.Inherit
import siebog.dnars.base.Copula.Similar
import siebog.dnars.base.Statement
import siebog.dnars.graph.DNarsEdge
import siebog.dnars.graph.DNarsEdge.wrap
import siebog.dnars.graph.DNarsGraph
import siebog.dnars.graph.DNarsVertex
import siebog.dnars.graph.DNarsVertex.wrap
import com.tinkerpop.blueprints.Edge

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
	/**
	 * Returns a list of conclusions for the given set of input statements.
	 */
	def conclusions(graph: DNarsGraph, input: Array[Statement]): Array[Statement] = {
		var res = ListBuffer[Statement]()
		for (st <- input) {
			res ++= deduction_analogy(graph, st)
			res ++= analogy_resemblance(graph, st)
			res ++= abduction_comparison_analogy(graph, st)
			res ++= induction_comparison(graph, st)
			res ++= analogy_inv(graph, st)
		}
		res.toArray
	}

	def conclusions(graph: DNarsGraph, input: Statement): Array[Statement] =
		conclusions(graph, Array(input))

	/**
	 * Includes the given set of statements in the graph, applying forward inference rules along the way.
	 */
	def include(graph: DNarsGraph, input: Array[Statement]): Unit = {
		val c = conclusions(graph, input)
		graph.statements.addAll(input.toList ::: c.toList)
	}

	// in the following functions, the first premise is taken from the graph,
	// while the second premise is passed as a parameter

	// M -> P  
	//		S -> M	=> S -> P ded 
	//		S ~ M	=> S -> P ana
	def deduction_analogy(graph: DNarsGraph, judgement: Statement): List[Statement] = {
		var res = ListBuffer[Statement]()
		for (st <- judgement.allForms) {
			val m = graph.getV(st.pred)
			if (m != None) {
				val edges = m.outE(Inherit).toList
				for (e <- edges) {
					val vertex: DNarsVertex = e.getVertex(Direction.IN)
					val p = vertex.term
					if (st.subj != p) {
						val derivedTruth =
							if (st.copula == Inherit)
								e.truth.deduction(st.truth)
							else
								e.truth.analogy(st.truth, false)
						val derived = Statement(st.subj, Inherit, p, derivedTruth)
						append(graph, res, derived)
					}
				}
			}
		}
		res.toList
	}

	def deduction_analogy(graph: DNarsGraph, judgements: Seq[Statement]): List[Statement] = {
		val res = ListBuffer[Statement]()
		for (j <- judgements)
			res ++= deduction_analogy(graph, j)
		res.toList
	}

	// M ~ P ::
	//		S -> M	=> S -> P ana'
	//		S ~ M	=> S ~ P res
	def analogy_resemblance(graph: DNarsGraph, judgement: Statement): List[Statement] = {
		var res = ListBuffer[Statement]()
		for (st <- judgement.allForms) {
			val m = graph.getV(st.pred)
			if (m != None) {
				val edges = m.outE(Similar).toList
				for (e <- edges) {
					val vertex: DNarsVertex = e.getVertex(Direction.IN)
					val p = vertex.term
					if (st.subj != p) {
						val edge: DNarsEdge = e
						val derived =
							if (st.copula == Inherit)
								Statement(st.subj, Inherit, p, edge.truth.analogy(st.truth, true))
							else
								Statement(st.subj, Similar, p, edge.truth.resemblance(st.truth))
						append(graph, res, derived)
					}
				}
			}
		}
		res.toList
	}

	// P -> M 
	//		S -> M	=> S -> P abd, S ~ P cmp
	//		S ~ M 	=> P -> S ana
	def abduction_comparison_analogy(graph: DNarsGraph, judgement: Statement): List[Statement] = {
		var res = ListBuffer[Statement]()
		for (st <- judgement.allForms) {
			val m = graph.getV(st.pred)
			if (m != None) {
				val edges = m.inE(Inherit).toList
				for (e <- edges) {
					val vertex: DNarsVertex = e.getVertex(Direction.OUT)
					val p = vertex.term
					if (st.subj != p) {
						val edge: DNarsEdge = e
						if (st.copula == Inherit) {
							val abd = edge.truth.abduction(st.truth)
							append(graph, res, Statement(st.subj, Inherit, p, abd))
							val cmp = edge.truth.comparison(st.truth)
							append(graph, res, Statement(st.subj, Similar, p, cmp))
						} else {
							val ana = edge.truth.analogy(st.truth, false)
							append(graph, res, Statement(p, Inherit, st.subj, ana))
						}
					}
				}
			}
		}
		res.toList
	}

	def abduction_comparison_analogy(graph: DNarsGraph, judgements: Seq[Statement]): List[Statement] = {
		val res = ListBuffer[Statement]()
		for (j <- judgements)
			res ++= abduction_comparison_analogy(graph, j)
		res.toList
	}

	// M -> P, M -> S => S -> P ind, S ~ P cmp
	def induction_comparison(graph: DNarsGraph, judgement: Statement): List[Statement] = {
		var res = ListBuffer[Statement]()
		for (st <- judgement.allForms) {
			if (st.copula == Inherit) {
				val m = graph.getV(st.subj)
				if (m != None) {
					val edges = m.outE(Inherit).toList
					for (e <- edges) {
						val vertex: DNarsVertex = e.getVertex(Direction.IN)
						val p = vertex.term
						if (st.pred != p) {
							val edge: DNarsEdge = e
							val ind = edge.truth.induction(st.truth)
							append(graph, res, Statement(st.pred, Inherit, p, ind))
							val cmp = edge.truth.comparison(st.truth)
							append(graph, res, Statement(st.pred, Similar, p, cmp))
						}
					}
				}
			}
		}
		res.toList
	}

	// M ~ P, M -> S => P -> S ana'
	def analogy_inv(graph: DNarsGraph, judgement: Statement): List[Statement] = {
		var res = ListBuffer[Statement]()
		for (st <- judgement.allForms) {
			if (st.copula == Inherit) {
				val m = graph.getV(st.subj)
				if (m != None) {
					val edges = m.outE(Similar).toList
					for (e <- edges) {
						val vertex: DNarsVertex = e.getVertex(Direction.IN)
						val p = vertex.term
						if (st.pred != p) {
							val edge: DNarsEdge = e
							val ana = edge.truth.analogy(st.truth, true)
							append(graph, res, Statement(p, Inherit, st.pred, ana))
						}
					}
				}
			}
		}
		res.toList
	}

	private implicit def opt2sv(v: Option[Vertex]): ScalaVertex =
		ScalaVertex.wrap(v.get)

	private implicit def edge2dnars(e: Edge): DNarsEdge =
		DNarsEdge.wrap(e)

	private def append(graph: DNarsGraph, res: ListBuffer[Statement], st: Statement): Unit =
		if (graph.statements.validStatement(st))
			res += st
}