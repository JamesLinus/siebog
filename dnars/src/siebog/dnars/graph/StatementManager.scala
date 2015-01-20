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
import siebog.dnars.base.Connector.ExtImage
import siebog.dnars.base.Connector.IntImage
import siebog.dnars.base.Connector.Product
import siebog.dnars.base.Copula.Inherit
import siebog.dnars.base.Copula.Similar
import siebog.dnars.base.Statement
import siebog.dnars.events.EventKind
import siebog.dnars.events.EventPayload
import siebog.dnars.graph.Wrappers.edge2DNarsEdge
import siebog.dnars.graph.Wrappers.vertex2DNarsVertex
import siebog.dnars.base.Truth
import com.tinkerpop.blueprints.Edge
import com.tinkerpop.blueprints.Graph
import siebog.dnars.events.EventManager
import siebog.dnars.base.StatementParser

/**
 * A set of functions for manipulating statements in the theGraph.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
trait StatementManager extends DNarsGraphApi {
	override def add(st: Statement): Unit = if (validStatement(st)) {
		// TODO : when adding statements, consider that "S~P <=> S->P & P->S" and "S~P => S->P"
		val existing = getE(st)
		existing match {
			case Some(e) =>
				reviseExistingStatement(st, e)
				addEvent(new EventPayload(EventKind.UPDATED, st.toString))
			case None =>
				addNewStatement(st)
				addEvent(new EventPayload(EventKind.ADDED, st.toString))
		}
	}

	override def add(st: String): Unit =
		try {
			add(StatementParser(st))
		} catch {
			case e: Throwable =>
				throw new IllegalArgumentException(e.getMessage)
		}

	override def add(st: Seq[Statement]): Unit = {
		paused = true
		try {
			st.foreach { add(_) }
		} finally {
			paused = false
		}
	}

	/**
	 * Checks if the given statement is valid. For now, it only checks compound terms with product connectors.
	 */
	override def validStatement(st: Statement): Boolean = (st.subj, st.copula, st.pred) match {
		case (CompoundTerm(Product, _), Inherit, CompoundTerm(_, _)) =>
			false // predicate should be an atomic term
		case (CompoundTerm(_, _), Inherit, CompoundTerm(Product, _)) =>
			false // subject should be an atomic term
		case (CompoundTerm(ExtImage, _), _, _) =>
			false // extensional image on the intensional side
		case (_, _, CompoundTerm(IntImage, _)) =>
			false // intensional image on the extensional side
		case _ =>
			true
	}

	override def assert(st: Statement): Unit = {
		getE(st) match {
			case None =>
				throw new IllegalArgumentException("Not found.");
			case Some(e) =>
				if (!e.truth.closeTo(st.truth))
					throw new IllegalArgumentException(e.truth.toString)
		}
	}

	private def reviseExistingStatement(st: Statement, e: Edge): Unit = {
		val revisedTruth = e.truth.revision(st.truth)
		e.truth = revisedTruth
		if (st.copula == Similar)
			reviseOppositeEdge(st, revisedTruth)
		else
			reviseImages(st, revisedTruth)
	}

	private def reviseOppositeEdge(st: Statement, revisedTruth: Truth): Unit = {
		val oppStat = Statement(st.pred, Similar, st.subj, st.truth)
		val edge = getE(oppStat).get
		edge.truth = revisedTruth
	}

	private def reviseImages(st: Statement, truth: Truth): Unit = {
		val images = st.allImages.tail.iterator
		while (images.hasNext) {
			val edge = getE(images.next).get
			edge.truth = truth
		}
	}

	private def addNewStatement(st: Statement): Unit = {
		addE(st)
		if (st.copula == Similar) {
			val opposite = Statement(st.pred, Similar, st.subj, st.truth)
			addE(opposite)
		} else
			addNewImages(st)
	}

	private def addNewImages(st: Statement): Unit = {
		val images = st.allImages.tail.iterator
		while (images.hasNext)
			addE(images.next)
	}

	private def addE(st: Statement): Unit =
		addE(getOrAddV(st.subj), st.copula, getOrAddV(st.pred), st.truth)

	/**
	 * For testing purposes only. Returns all statements in the graph.
	 */
	def getAll: List[Statement] = {
		val edges = E.toList
		val n = edges.size
		val res = new ListBuffer[Statement]()
		for (e <- edges) {
			val s = e.getVertex(Direction.OUT)
			val p = e.getVertex(Direction.IN)
			val statement = Statement(s.term, e.copula, p.term, e.truth)
			if (statement.pack.lengthCompare(0) == 0)
				res += statement
		}
		res.toList
	}
}