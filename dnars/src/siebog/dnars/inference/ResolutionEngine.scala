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

import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Edge
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.gremlin.scala.GremlinScalaPipeline

import siebog.dnars.base.AtomicTerm.Question
import siebog.dnars.base.Copula.Similar
import siebog.dnars.base.Statement
import siebog.dnars.base.Term
import siebog.dnars.base.Truth
import siebog.dnars.graph.DNarsGraph
import siebog.dnars.graph.DNarsGraphAPI
import siebog.dnars.graph.Wrappers.edge2DNarsEdge
import siebog.dnars.graph.Wrappers.vertex2DNarsVertex
import siebog.dnars.inference.forward.ForwardInferenceEngine

/**
 * Resolution engine for answering questions.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
trait ResolutionEngine extends DNarsGraphAPI {

	def answer(question: Statement, limit: Int = 1): Array[Statement] = {
		if (question.subj == Question) {
			val list = getBestSubjects(question.pred, question.copula, limit).toArray // answerMissingSubject(question, limit)
			for (s <- list) yield Statement(s, question.copula, question.pred, Truth(1.0, 0.9))
		} else if (question.pred == Question) {
			val list = getBestPredicates(question.subj, question.copula, limit).toArray // answerMissingPredicate(question, limit)
			for (p <- list) yield Statement(question.subj, question.copula, p, Truth(1.0, 0.9))
		} else {
			val candidates = for (q <- question.allImages()) yield inferBackwards(q)
			candidates.flatten.take(limit).toArray
		}
	}

	def hasAnswer(question: Statement): Boolean =
		getE(question) != None

	private def answerMissingSubject(question: Statement, limit: Int): Array[Statement] = {
		val candidates =
			getAnswersForMissingSubject(question.pred, question.copula) :::
				(if (question.copula == Similar)
					getAnswersForMissingPredicate(question.pred, question.copula)
				else
					Nil)
		candidates.sorted.take(limit).map(_.toStatementWithMissingSubj(question.pred, question.copula)).toArray
	}

	private def answerMissingPredicate(question: Statement, limit: Int): Array[Statement] = {
		val candidates =
			getAnswersForMissingPredicate(question.subj, question.copula) :::
				(if (question.copula == Similar)
					getAnswersForMissingSubject(question.subj, question.copula)
				else
					Nil)
		candidates.sorted.take(limit).map(_.toStatementWithMissingPred(question.subj, question.copula)).toArray
	}

	private def getAnswersForMissingSubject(pred: Term, copula: String): List[Answer] =
		getV(pred) match {
			case Some(vert) =>
				createPossibleAnswers(vert.inE(copula), Direction.OUT)
			case None =>
				List()
		}

	private def getAnswersForMissingPredicate(subj: Term, copula: String): List[Answer] =
		getV(subj) match {
			case Some(vert) =>
				createPossibleAnswers(vert.outE(copula), Direction.IN)
			case None => // does not exist
				List()
		}

	private def createPossibleAnswers(pipe: GremlinScalaPipeline[Vertex, Edge], dir: Direction): List[Answer] = {
		pipe.map { e =>
			val term = e.getVertex(dir).term
			val truth = e.truth
			val expectation = truth.expectation
			val simplicity = 1.0 / term.complexity
			new Answer(term, truth, expectation, simplicity)
		}.toList()
	}
}

class Answer(val term: Term, val truth: Truth, val expectation: Double, val simplicity: Double) extends Comparable[Answer] {
	val EPSILON = 0.001

	override def compareTo(other: Answer): Int = {
		val expectationDifference = math.abs(expectation - other.expectation)
		if (expectationDifference < EPSILON)
			chooseSimplerAnswer(other)
		else
			chooseHigherExpectationAndSimplicity(other)
	}

	def toStatementWithMissingSubj(pred: Term, copula: String): Statement =
		Statement(term, copula, pred, truth)

	def toStatementWithMissingPred(subj: Term, copula: String): Statement =
		Statement(subj, copula, term, truth)

	private def chooseSimplerAnswer(other: Answer): Int = {
		val simp = simplicity - other.simplicity
		if (math.abs(simp) < EPSILON) term.compareTo(other.term)
		else if (simp < 0) -1
		else 1
	}

	private def chooseHigherExpectationAndSimplicity(other: Answer): Int = {
		val p1 = expectation * simplicity
		val p2 = other.expectation * other.simplicity
		val diff = p1 - p2
		if (math.abs(diff) < EPSILON) term.compareTo(other.term)
		else if (diff < 0) 1
		else -1
	}
}