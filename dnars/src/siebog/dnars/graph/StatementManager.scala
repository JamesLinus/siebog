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

import com.tinkerpop.blueprints.Direction

import siebog.dnars.base.CompoundTerm
import siebog.dnars.base.Connector.Product
import siebog.dnars.base.Copula.Inherit
import siebog.dnars.base.Copula.Similar
import siebog.dnars.base.Statement
import siebog.dnars.events.EventKind
import siebog.dnars.events.EventPayload
import siebog.dnars.graph.DNarsEdge.wrap
import siebog.dnars.graph.DNarsVertex.wrap

/**
 * A set of functions for manipulating statements in the graph.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class StatementManager(val graph: DNarsGraph) {
	def add(st: Statement): Unit = if (validStatement(st)) {
		// TODO : when adding statements, consider that "S~P <=> S->P & P->S" and "S~P => S->P"
		val existing = graph.getE(st)
		existing match {
			case Some(e) => // already exists, apply revision
				val edge: DNarsEdge = e
				val truth = edge.truth.revision(st.truth)
				edge.truth = truth
				if (st.copula == Similar) {
					// update in the opposite direction as well
					val invStat = Statement(st.pred, Similar, st.subj, st.truth)
					val invEdge: DNarsEdge = graph.getE(invStat).get
					invEdge.truth = truth
				} else {
					// are there any structural transformations?
					st.pack match {
						case List(su1, su2) =>
							val e1: DNarsEdge = graph.getE(su1).get
							val e2: DNarsEdge = graph.getE(su2).get
							e1.truth = truth
							e2.truth = truth
						case _ => st.unpack match {
							case List(sp1, sp2) =>
								val e1: DNarsEdge = graph.getE(sp1).get
								val e2: DNarsEdge = graph.getE(sp2).get
								e1.truth = truth
								e2.truth = truth
							case _ =>
						}
					}
				}
				val event = new EventPayload(EventKind.UPDATED, st.toString)
				graph.eventManager.addEvent(event)
			case None =>
				addE(st)
				if (st.copula == Similar)
					addE(Statement(st.pred, Similar, st.subj, st.truth))
				else {
					// structural transformations?
					val unpacked = unpackAndAdd(graph, st)
					if (!unpacked)
						packAndAdd(graph, st)
				}
				val event = new EventPayload(EventKind.ADDED, st.toString)
				graph.eventManager.addEvent(event)
		}
	}

	def addAll(st: Seq[Statement]): Unit = {
		graph.eventManager.paused = true
		try {
			for (s <- st)
				add(s)
		} finally {
			graph.eventManager.paused = false
		}
	}

	/**
	 * Checks if the given statement is valid. For now, it only checks compound terms with product connectors.
	 */
	def validStatement(st: Statement): Boolean = (st.subj, st.copula, st.pred) match {
		case (CompoundTerm(Product, _), Inherit, CompoundTerm(_, _)) =>
			false // predicate should be an atomic term
		case (CompoundTerm(_, _), Inherit, CompoundTerm(Product, _)) =>
			false // subject should be an atomic term
		//case (CompoundTerm(Product, _), Similar, AtomicTerm(_)) =>
		//	false // copula should be inheritance
		//case (AtomicTerm(_), Similar, CompoundTerm(Product, _)) =>
		//	false // copula should be inheritance
		case _ =>
			true
	}

	/**
	 * Checks if the given statement exists in the graph.
	 *
	 * @throws IllegalArgumentException if the statement is not found, or if it has different truth-value.
	 */
	def assert(st: Statement): Unit = {
		graph.getE(st) match {
			case None =>
				throw new IllegalArgumentException("Not found.");
			case Some(e) =>
				val edge: DNarsEdge = e
				if (!edge.truth.closeTo(st.truth))
					throw new IllegalArgumentException(edge.truth.toString)
		}
	}

	/**
	 * For testing purposes only. Returns all statements in the graph.
	 */
	def getAll: List[Statement] = {
		val edges = graph.E.toList
		val n = edges.size
		val st = new ListBuffer[Statement]()
		for (e <- edges) {
			val s: DNarsVertex = e.getVertex(Direction.OUT)
			val p: DNarsVertex = e.getVertex(Direction.IN)
			val edge: DNarsEdge = e
			st += Statement(s.term, edge.copula, p.term, edge.truth)
		}
		st.toList
	}

	/**
	 * Unpacks the given statement and adds its images to the graph.
	 *
	 * @return true if the statement could be transformed, false otherwise.
	 */
	def unpackAndAdd(graph: DNarsGraph, st: Statement): Boolean = st.unpack match {
		case List(st1, st2) =>
			addE(st1)
			addE(st2)
			true
		case _ =>
			false
	}

	def packAndAdd(graph: DNarsGraph, st: Statement): Boolean = st.pack match {
		case List(st1, st2) =>
			addE(st1)
			addE(st2)
			true
		case _ =>
			false
	}

	private def addE(st: Statement): Unit =
		graph.addE(graph.getOrAddV(st.subj), st.copula, graph.getOrAddV(st.pred), st.truth)
}