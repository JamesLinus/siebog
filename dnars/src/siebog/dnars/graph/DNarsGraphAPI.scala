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

import siebog.dnars.base.Term
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.GraphQuery
import com.tinkerpop.blueprints.Edge
import siebog.dnars.base.Truth
import siebog.dnars.base.Statement
import siebog.dnars.events.EventPayload
import com.tinkerpop.gremlin.scala.GremlinScalaPipeline

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
trait DNarsGraphAPI {
	def query(): GraphQuery

	def addV(id: Any): Vertex

	def E(): GremlinScalaPipeline[Edge, Edge]

	def getV(term: Term): Option[Vertex]

	def conclusions(input: Array[Statement]): Array[Statement]

	def conclusions(input: Statement): Array[Statement]

	def include(input: Array[Statement]): Unit

	def getOrAddV(term: Term): Vertex

	def addE(subj: Vertex, copula: String, pred: Vertex, truth: Truth): Edge

	def getE(st: Statement): Option[Edge]

	def add(st: Statement): Unit

	def add(st: String): Unit

	def add(st: Seq[Statement]): Unit

	def validStatement(st: Statement): Boolean

	def assertStatementExists(st: Statement): Unit

	def answer(question: Statement, limit: Int = 1): Array[Statement]

	def hasAnswer(question: Statement): Boolean

	def inferBackwards(question: Statement): List[Statement]

	def addEvent(event: EventPayload): Unit

	var paused: Boolean
}