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

	override def answer(question: Statement, limit: Int = 1): Array[Term] = {
		if (question.subj == Question) 
			getBestSubjects(question.pred, question.copula, limit).toArray 
		else if (question.pred == Question)
			getBestPredicates(question.subj, question.copula, limit).toArray 
		else
			throw new IllegalArgumentException("The question should be either ? -> P or S -> ?")
	}
	
	override def backwardInference(question: Statement, limit: Int = 1): Array[Statement] = {
		val candidates = 
			for (q <- question.allImages()) 
				yield inferBackwards(q)
		candidates.flatten.take(limit).toArray
	}
	
	private def inferBackwards(question: Statement): List[Statement] = {
		if (getV(question.subj) == None || getV(question.pred) == None)
			List()
		else
			getE(question) match {
				case Some(edge) =>
					List(Statement(question.subj, question.copula, question.pred, edge.truth))
				case None =>
					inferUsingForwardInference(question)
			}
	}

	private def inferUsingForwardInference(question: Statement): List[Statement] = {
		// construct derived questions such that { P, Q } |- DerivedQ
		val q = Statement(question.subj, question.copula, question.pred, Truth(1.0, 0.9))
		val derivedQuestions = conclusions(Array(q))
		// check if the starting question can be derived from { P, DerivedQ }
		val candidates = conclusions(derivedQuestions)
		candidates.toList
			.filter(c => c.subj == question.subj && c.pred == question.pred)
			.sortWith((a, b) => a.truth.expectation > b.truth.expectation)
	}

	def hasAnswer(question: Statement): Boolean =
		getE(question) != None
}