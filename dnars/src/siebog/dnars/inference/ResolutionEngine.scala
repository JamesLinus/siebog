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

import siebog.dnars.graph.DNarsGraph
import siebog.dnars.base.Statement
import siebog.dnars.base.AtomicTerm._
import siebog.dnars.base.Copula._
import siebog.dnars.base.Truth
import siebog.dnars.graph.DNarsVertex
import com.tinkerpop.blueprints.Direction
import siebog.dnars.graph.DNarsEdge
import siebog.dnars.base.Term
import com.tinkerpop.gremlin.scala.GremlinScalaPipeline
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.Edge

/**
 * Resolution engine for answering questions.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class ResolutionEngine(val graph: DNarsGraph) {
	/**
	 * Answers questions in form of "S copula ?" and "? copula P"
	 */
	def answer(question: Statement): Option[Term] = {
		val copula = question.copula
		if (question.subj == Question) { // ? -> P, ? ~ P
			val result = answerForPredicate(question.pred, copula)
			if (result == None && copula == Similar) // ? ~ P, reflexive
				answerForSubject(question.pred, copula)
			else
				result
		} else if (question.pred == Question) { // S -> ?, S ~ ?
			val result = answerForSubject(question.subj, copula)
			if (result == None && copula == Similar) // S ~ ?, reflexive
				answerForPredicate(question.subj, copula)
			else
				result
		} else
			throw new IllegalArgumentException("Bad question format, should be 'S copula ?' or '? copula P'")
	}

	/**
	 * Checks if there is an answer for a question in form of "S copula P"
	 */
	def exists(question: Statement): Boolean = {
		val st = Statement(question.subj, question.copula, question.pred, Truth(1.0, 0.9))
		hasAnswer(Set(st))
	}

	private def hasAnswer(questions: Set[Statement]): Boolean = {
		try {
			val q = questions.head
			graph.getE(q) match {
				case Some(_) =>
					true
				case None =>
					val derivedQuestons = ForwardInference.conclusions(graph, List(q))
					hasAnswer(derivedQuestons.toSet ++ questions.tail)
			}
		} catch {
			case _: NoSuchElementException => false
		}
	}

	private def answerForPredicate(pred: Term, copula: String): Option[Term] =
		graph.getV(pred) match {
			case Some(vert) =>
				val pipe = DNarsVertex.wrap(vert).inE(copula)
				bestAnswer(pipe, Direction.OUT)
			case None => // does not exist
				None
		}

	private def answerForSubject(subj: Term, copula: String): Option[Term] =
		graph.getV(subj) match {
			case Some(vert) =>
				val pipe = DNarsVertex.wrap(vert).outE(copula)
				bestAnswer(pipe, Direction.IN)
			case None => // does not exist
				None
		}

	private def bestAnswer(pipe: GremlinScalaPipeline[Vertex, Edge], dir: Direction): Option[Term] = {
		val candidates = pipe.map { e =>
			val term = DNarsVertex.wrap(e.getVertex(dir)).term
			val expectation = DNarsEdge.wrap(e).truth.expectation
			val simplicity = 1.0 / term.complexity
			new Answer(term, expectation, simplicity)
		}.toList

		candidates.sorted match {
			case head :: _ => Some(head.term)
			case _ => None
		}
	}
}

class Answer(val term: Term, val expectation: Double, val simplicity: Double) extends Comparable[Answer] {
	override def compareTo(other: Answer): Int = {
		val EPSILON = 0.001
		val absDiff = math.abs(expectation - other.expectation)
		// if the two competing answers have the same e, the simpler answer is chosen
		if (absDiff < EPSILON) {
			val simp = simplicity - other.simplicity
			if (math.abs(simp) < EPSILON) term.compareTo(other.term)
			else if (simp < 0) -1
			else 1
		} else {
			// if the expectations differ, the answer with higher e * s is selected
			val p1 = expectation * simplicity
			val p2 = other.expectation * other.simplicity
			val diff = p1 - p2
			if (math.abs(diff) < EPSILON) term.compareTo(other.term)
			else if (diff < 0) 1
			else -1
		}
	}
}