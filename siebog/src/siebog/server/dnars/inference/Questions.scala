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

package siebog.server.dnars.inference
	
import siebog.server.dnars.graph.DNarsGraph
import siebog.server.dnars.base.Statement
import siebog.server.dnars.base.AtomicTerm._
import siebog.server.dnars.base.Copula._
import siebog.server.dnars.graph.DNarsVertex
import com.tinkerpop.blueprints.Direction
import siebog.server.dnars.graph.DNarsEdge
import siebog.server.dnars.base.Term
import com.tinkerpop.gremlin.scala.GremlinScalaPipeline
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.Edge

/**
 * Local inference for answering simple questions, with "?" as either 
 * the subject or the predicate.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a> 
 */

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
		}
		else {
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

object Questions {
	def answer(graph: DNarsGraph, question: Statement): Option[Term] = {
		val copula = question.copula
		if (question.subj == Question) { // ? -> P, ? ~ P
			val result = answerForPredicate(graph, question.pred, copula)
			if (result == None && copula == Similar) // ? ~ P, reflexive
				answerForSubject(graph, question.pred, copula)
			else
				result
		}
		else if (question.pred == Question) { // S -> ?, S ~ ?
			val result = answerForSubject(graph, question.subj, copula)
			if (result == None && copula == Similar) // S ~ ?, reflexive
				answerForPredicate(graph, question.subj, copula)
			else
				result
		}
		else
			throw new IllegalArgumentException("Questions should have '?' as either the subject or the predicate")
	}
	
	def exists(graph: DNarsGraph, question: Statement): Boolean = {
		// both terms should exist
		graph.getV(question.subj) match {
			case None => false
			case Some(s) =>
				val subj: DNarsVertex = s
				graph.getV(question.pred) match {
					case None => false
					case Some(pred) =>
						val allEdges = subj.startPipe.as("x").outE
						// if the question is a sim, we can traverse only over sim copulas
						val allowedEdges = 
							if (question.copula == Similar)
								allEdges.filter(e => e.getLabel == Similar)
							else
								allEdges
						allowedEdges.inV.loop("x", { lp => lp.getObject != pred }).hasNext
				}
		}
	}
	
	private def answerForPredicate(graph: DNarsGraph, pred: Term, copula: String): Option[Term] =
		graph.getV(pred) match {
			case Some(vert) =>
				val pipe = DNarsVertex.wrap(vert).inE(copula)
				bestAnswer(pipe, Direction.OUT)
			case None => // does not exist
				None
		}

	private def answerForSubject(graph: DNarsGraph, subj: Term, copula: String): Option[Term] =
		graph.getV(subj) match {
			case Some(vert) =>
				val pipe = DNarsVertex.wrap(vert).outE(copula)
				bestAnswer(pipe, Direction.IN)
			case None => // does not exist
				None
		}
	
	private def bestAnswer(pipe: GremlinScalaPipeline[Vertex, Edge], dir: Direction): Option[Term] = {
		val candidates = pipe.map { e => {
			val term = DNarsVertex.wrap(e.getVertex(dir)).term
			val expectation = DNarsEdge.wrap(e).truth.expectation
			val simplicity = 1.0 / term.complexity
			new Answer(term, expectation, simplicity)
		} }.toList
		
		candidates.sorted match {
			case head :: _ => Some(head.term)
			case _ => None
		}
	}
}