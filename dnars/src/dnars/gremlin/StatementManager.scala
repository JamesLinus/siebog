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

package dnars.gremlin

import dnars.base.Statement
import dnars.base.CompoundTerm
import dnars.base.Connector._
import dnars.base.Copula._
import dnars.base.AtomicTerm
import dnars.base.AtomicTerm._
import dnars.base.Term
import dnars.base.Truth
import com.tinkerpop.blueprints.Direction

/**
 * A set of functions for manipulating statements in the graph.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class StatementManager(val graph: DNarsGraph) {

	def add(st: Statement): Unit = {
		val existing = graph.getE(st)
		existing match {
			case Some(e) => // already exists, apply revision
				val edge: DNarsEdge = e
				val truth = edge.truth.revision(st.truth)
				edge.truth = truth
				// are there any structural transformations?
				unpack(st) match {
					case List(su1, su2) => 
						val e1: DNarsEdge = graph.getE(su1).get
						val e2: DNarsEdge = graph.getE(su2).get
						e1.truth = truth
						e2.truth = truth
					case _ => pack(st) match {
						case List(sp1, sp2) =>
							val e1: DNarsEdge = graph.getE(sp1).get
							val e2: DNarsEdge = graph.getE(sp2).get
							e1.truth = truth
							e2.truth = truth
						case _ =>
					}
				}
			case None =>
				addE(st)
				if (st.copula == Inherit) { 
					// structural transformations?
					val unpacked = unpackAndAdd(graph, st)
					if (!unpacked)
						packAndAdd(graph, st)
				}
		}
	}

	/**
	 * Checks if the given statement exists in the graph.
	 *
	 * @throws IllegalArgumentException if the statement is not found, or if it has different truth-value.
	 */
	def check(st: Statement): Unit = {
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
	def getAll: Array[Statement] = {
		val edges = graph.E.toList
		val n = edges.size
		val st = new Array[Statement](n)
		var i = 0
		for (e <- edges) {
			val s: DNarsVertex = e.getVertex(Direction.OUT)
			val p: DNarsVertex = e.getVertex(Direction.IN)
			val edge: DNarsEdge = e
			st(i) = Statement(s.term, edge.copula, p.term, edge.truth)
			i += 1
		}
		st
	}

	/**
	 * Performs structural transformation of a statement that includes compound
	 * terms with the product connector (e.g. "(cat x bird) -> eat" or 
	 * "dissolve -> (water x salt)").
	 *
	 * @return List of 2 images, or an empty list if the input statement does not 
	 * match any of the given forms.
	 */
	def unpack(st: Statement): List[Statement] = st match {
		// (cat x bird) -> eat
		case Statement(CompoundTerm(Product, List(t1, t2)), Inherit, pred @ AtomicTerm(_), truth) =>
			// cat -> (/ eat * bird)
			val s1 = t1
			val p1 = CompoundTerm(ExtImage, List(pred, Placeholder, t2))
			// bird -> (/ eat cat *)
			val s2 = t2
			val p2 = CompoundTerm(ExtImage, List(pred, t1, Placeholder))
			//
			List(Statement(s1, Inherit, p1, truth), Statement(s2, Inherit, p2, truth))
		// eat -> (cat x bird)
		case Statement(subj @ AtomicTerm(_), Inherit, CompoundTerm(Product, List(t1, t2)), truth) =>
			// (\ eat * bird) -> cat
			val s1 = CompoundTerm(IntImage, List(subj, AtomicTerm.Placeholder, t2))
			val p1 = t1
			// (\ eat cat *) -> bird
			val s2 = CompoundTerm(IntImage, List(subj, t1, AtomicTerm.Placeholder))
			val p2 = t2
			//
			List(Statement(s1, Inherit, p1, truth), Statement(s2, Inherit, p2, truth))
		case _ => 
			List()
	}

	/**
	 * Unpacks the given statement and adds its images to the graph.
	 * 
	 * @return true if the statement could be transformed, false otherwise.
	 */
	def unpackAndAdd(graph: DNarsGraph, st: Statement): Boolean = unpack(st) match {
		case List(st1, st2) =>
			addE(st1)
			addE(st2)
			true
		case _ =>
			false
	}

	/**
	 * (Re)Combines intentional and extentional images back into statements, 
	 * e.g. "cat -> (/ eat * bird)" becomes "(x cat bird) -> eat)". The other 
	 * intentional/extentional image is created as well, in this example
	 * "bird -> (/ eat cat *)".
	 */
	def pack(st: Statement): List[Statement] = st match {
		// cat -> (/ eat * bird)  => (x cat bird) -> eat, bird -> (/ eat cat *) 
		case Statement(subj1 @ AtomicTerm(_), Inherit, CompoundTerm(ExtImage, List(rel @ AtomicTerm(_), Placeholder, subj2 @ AtomicTerm(_))), truth) =>
			val cp1 = CompoundTerm(Product, List(subj1, subj2))
			val cp2 = CompoundTerm(ExtImage, List(rel, subj1, Placeholder))
			//
			List(Statement(cp1, Inherit, rel, truth), Statement(subj2, Inherit, cp2, truth))
		// bird -> (/ eat cat *) => (x cat bird) -> eat, cat -> (/ eat * bird)
		case Statement(subj2 @ AtomicTerm(_), Inherit, CompoundTerm(ExtImage, List(rel @ AtomicTerm(_), subj1 @ AtomicTerm(_), Placeholder)), truth) =>
			val cp1 = CompoundTerm(Product, List(subj1, subj2))
			val cp2 = CompoundTerm(ExtImage, List(rel, Placeholder, subj2))
			//
			List(Statement(cp1, Inherit, rel, truth), Statement(subj1, Inherit, cp2, truth))
		// (\ dissolve * solid) -> liquid => dissolve -> (x liquid solid), (\ dissolve liquid *) -> solid
		case Statement(CompoundTerm(IntImage, List(rel @ AtomicTerm(_), Placeholder, subj2 @ AtomicTerm(_))), Inherit, subj1 @ AtomicTerm(_), truth) =>
			val cp1 = CompoundTerm(Product, List(subj1, subj2))
			val cp2 = CompoundTerm(IntImage, List(rel, subj1, Placeholder))
			//
			List(Statement(rel, Inherit, cp1, truth), Statement(cp2, Inherit, subj2, truth))
		// (\ dissolve liquid *) -> solid => dissolve -> (x liquid solid), (\ dissolve * solid) -> liquid
		case Statement(CompoundTerm(IntImage, List(rel @ AtomicTerm(_), subj1 @ AtomicTerm(_), Placeholder)), Inherit, subj2 @ AtomicTerm(_), truth) =>
			val cp1 = CompoundTerm(Product, List(subj1, subj2))
			val cp2 = CompoundTerm(IntImage, List(rel, Placeholder, subj2))
			//
			List(Statement(rel, Inherit, cp1, truth), Statement(cp2, Inherit, subj1, truth))
		case _ => 
			List()
	}
	
	def packAndAdd(graph: DNarsGraph, st: Statement): Boolean = pack(st) match {
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