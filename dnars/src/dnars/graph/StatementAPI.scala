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

import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Edge

import dnars.base.CompoundTerm
import dnars.base.Connector.ExtImage
import dnars.base.Connector.IntImage
import dnars.base.Connector.Product
import dnars.base.Copula.Inherit
import dnars.base.Copula.Similar
import dnars.base.Statement
import dnars.base.StatementParser
import dnars.base.Truth
import dnars.events.Added
import dnars.events.EventPayload
import dnars.events.Updated
import dnars.graph.Wrappers.edge2DNarsEdge
import dnars.graph.Wrappers.vertex2DNarsVertex

/**
 * Subset of the DNarsGraph API focused on NAL statements.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
trait StatementAPI extends DNarsGraphAPI {
	override def add(st: Statement): Unit = if (validStatement(st)) {
		// TODO : when adding statements, consider that "S~P <=> S->P & P->S" and "S~P => S->P"
		val existing = getE(st)
		existing match {
			case Some(e) =>
				reviseExistingStatement(st, e)
				addEvent(new EventPayload(Updated(), st.toString))
			case None =>
				addNewStatement(st)
				addEvent(new EventPayload(Added(), st.toString))
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

	override def assertStatementExists(st: Statement): Unit = {
		getE(st) match {
			case None =>
				throw new IllegalArgumentException(s"Statement does not exist: $st")
			case Some(e) =>
				if (!e.truth.closeTo(st.truth)) {
					val msg = s"Invalid truth value, expected ${st.truth}, found ${e.truth}"
					throw new IllegalArgumentException(msg)
				}
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
		val oppositeStatement = Statement(st.pred, Similar, st.subj, st.truth)
		setTruthValue(oppositeStatement, revisedTruth)
	}

	private def reviseImages(st: Statement, truth: Truth): Unit = {
		val images = st.allImages.tail.iterator
		while (images.hasNext)
			setTruthValue(images.next(), truth)
	}

	private def setTruthValue(st: Statement, truth: Truth): Unit = {
		getE(st) match {
			case Some(e) =>
				e.truth = truth
			case None =>
				throw new IllegalArgumentException("No edge for statement " + st)
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