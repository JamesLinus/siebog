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

package siebog.dnars

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import siebog.dnars.base.Copula
import siebog.dnars.base.Statement
import siebog.dnars.base.StatementParser
import siebog.dnars.graph.DNarsGraph
import scala.collection.mutable.ListBuffer

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
object DNarsTestUtils {
	val TEST_KEYSPACE = "TestUtils123"

	def createAndAdd(graph: DNarsGraph, statements: String*): List[Statement] = {
		for (str <- statements.toList) yield {
			val stat = StatementParser(str)
			graph.add(stat)
			stat
		}
	}

	def create(statements: String*): List[Statement] =
		for (str <- statements.toList)
			yield StatementParser(str)

	def assertSeq(expected: Seq[Statement], actual: Seq[Statement]): Unit = {
		try {
			assertEquals(expected.size, actual.size)
		} catch {
			case e: AssertionError =>
				actual.foreach { println(_) }
				throw e

		}
		// cannot use "contains" because of Truth similarities
		for (ste <- expected) {
			var found = false
			for (form <- ste.allImages)
				for (sta <- actual)
					if (form.equivalent(sta))
						found = true
			assertTrue("Statement " + ste + " not found.", found)
		}
	}

	def assertGraph(graph: DNarsGraph, expected: Seq[Statement]): Unit = {
		try {
			val actual = graph.getAll
			assertSeq(expected, actual)
		} catch {
			case ex: AssertionError =>
				graph.printEdges
				throw ex
		}
	}

	def invert(st: Statement): Statement = {
		if (!st.copula.equals(Copula.Similar))
			throw new IllegalArgumentException("Only similarity statements can be inverted.")
		Statement(st.pred, Copula.Similar, st.subj, st.truth)
	}
}