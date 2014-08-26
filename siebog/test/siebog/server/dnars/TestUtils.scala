/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under
 * the Apache License, Version 2.0 (the "License") you may not
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

package siebog.server.dnars

import siebog.server.dnars.base.Statement
import scala.collection.mutable.ListBuffer
import siebog.server.dnars.graph.DNarsGraph
import siebog.server.dnars.base.StatementParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import siebog.server.dnars.base.Copula
import scala.collection.mutable.ArrayBuffer

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
object TestUtils {
	val TEST_KEYSPACE = "TestUtils123"

	def createAndAdd(graph: DNarsGraph, statements: String*): Array[Statement] = {
		val res = new ArrayBuffer[Statement]()
		for (str <- statements) {
			val stat = StatementParser(str)
			graph.statements.add(stat)
			res += stat
		}
		res.toArray
	}

	def create(statements: String*): List[Statement] = {
		val res = new ListBuffer[Statement]()
		for (str <- statements)
			res += StatementParser(str)
		res.toList
	}

	def assertGraph(graph: DNarsGraph, kb: Seq[Statement], res: Seq[Statement]): Unit = {
		val expectedStatements = new ListBuffer[Statement]()
		expectedStatements ++= kb
		expectedStatements ++= res

		val graphStatements = graph.statements.getAll
		try {
			assertEquals(expectedStatements.size, graphStatements.length)
			for (st <- expectedStatements) {
				var found = false
				for (grStat <- graphStatements if !found)
					if (st.equivalent(grStat))
						found = true
				assertTrue("Statement " + st + " not found.", found)
			}
		} catch {
			case e: AssertionError =>
				graph.printEdges
				throw e
		}
	}

	def invert(st: Statement): Statement = {
		if (!st.copula.equals(Copula.Similar))
			throw new IllegalArgumentException("Only similarity statements can be inverted.")
		Statement(st.pred, Copula.Similar, st.subj, st.truth)
	}
}