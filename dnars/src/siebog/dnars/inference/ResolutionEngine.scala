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
import siebog.dnars.graph.StructuralTransform
import scala.collection.mutable.ListBuffer

/**
 * Resolution engine for answering questions.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
object ResolutionEngine {

	def answer(graph: DNarsGraph, question: Statement, limit: Int = 1): Array[Statement] = {
		val copula = question.copula
		(question.subj, question.pred) match {

			case (Question, p) => // ? copula P
				val answ1 = answerForPredicate(graph, p, copula, limit)
				val answ2 = if (copula == Similar) // ~ is reflexive
					answerForSubject(graph, question.pred, copula, limit)
				else
					Nil
				(answ1 ::: answ2)
					.take(limit)
					.map(a => Statement(a.term, copula, p, a.truth))
					.toArray

			case (s, Question) => // S copula ?
				val answ1 = answerForSubject(graph, question.subj, copula, limit)
				val answ2 = if (copula == Similar) //  ~ is reflexive
					answerForPredicate(graph, question.subj, copula, limit)
				else
					Nil
				(answ1 ::: answ2)
					.take(limit)
					.map(a => Statement(s, copula, a.term, a.truth))
					.toArray

			case (s, p) => // backward inference
				val answ1 = inferBackwards(graph, question, limit)
				// try with structural transformations (packing)
				val answ2 = StructuralTransform.pack(question) match {
					case List(q1, q2) =>
						inferBackwards(graph, q1, limit) ::: inferBackwards(graph, q2, limit)
					case _ =>
						List()
				}
				// try with structural transformations (unpacking)
				val answ3 = StructuralTransform.unpack(question) match {
					case List(q1, q2) =>
						inferBackwards(graph, q1, limit) ::: inferBackwards(graph, q2, limit)
					case _ =>
						List()
				}
				(answ1 ::: answ2 ::: answ3)
					.take(limit)
					.toArray
		}
	}

	def hasAnswer(graph: DNarsGraph, question: Statement): Boolean =
		graph.getE(question) != None

	private def answerForPredicate(graph: DNarsGraph, pred: Term, copula: String, limit: Int): List[Answer] =
		graph.getV(pred) match {
			case Some(vert) =>
				val pipe = DNarsVertex.wrap(vert).inE(copula)
				bestAnswer(pipe, Direction.OUT, limit)
			case None => // does not exist
				List()
		}

	private def answerForSubject(graph: DNarsGraph, subj: Term, copula: String, limit: Int): List[Answer] =
		graph.getV(subj) match {
			case Some(vert) =>
				val pipe = DNarsVertex.wrap(vert).outE(copula)
				bestAnswer(pipe, Direction.IN, limit)
			case None => // does not exist
				List()
		}

	private def bestAnswer(pipe: GremlinScalaPipeline[Vertex, Edge], dir: Direction, limit: Int): List[Answer] = {
		pipe.map { e =>
			val term = DNarsVertex.wrap(e.getVertex(dir)).term
			val truth = DNarsEdge.wrap(e).truth
			val expectation = truth.expectation
			val simplicity = 1.0 / term.complexity
			new Answer(term, truth, expectation, simplicity)
		}
			.toList
			.sorted
			.take(limit)
	}

	private def inferBackwards(graph: DNarsGraph, question: Statement, limit: Int): List[Statement] = {
		graph.getE(question) match {
			case Some(edge) => // answer is directly present in the graph 
				val e = DNarsEdge.wrap(edge)
				List(Statement(question.subj, question.copula, question.pred, e.truth))
			case None =>
				// make sure both terms exist in the graph
				if (graph.getV(question.subj) == None || graph.getV(question.pred) == None)
					List()
				else {
					// construct derived questions such that { P, Q } |- DerivedQ
					val q = Statement(question.subj, question.copula, question.pred, Truth(1.0, 0.9))
					val derivedQuestions = ForwardInference.conclusions(graph, Seq(q))
					// check if the starting question can be derived from { P, DerivedQ }
					val candidates = ForwardInference.conclusions(graph, derivedQuestions)
					candidates
						.filter(c => c.subj == question.subj && c.pred == question.pred)
						.sortWith((a, b) => a.truth.expectation > b.truth.expectation)
						.take(limit)
				}
		}
	}
}

class Answer(val term: Term, val truth: Truth, val expectation: Double, val simplicity: Double) extends Comparable[Answer] {
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