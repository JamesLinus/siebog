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

package siebog.dnars

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import siebog.dnars.base.Statement
import siebog.dnars.base.StatementParser
import siebog.dnars.graph.DNarsGraph
import siebog.dnars.graph.DNarsGraphFactory
import siebog.dnars.base.Copula

/**
 * Base class for DNARS tests. Provides utility functions.
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
abstract class DNarsTestBase {
	val TEST_KEYSPACE = "TestUtils123"
	var graph: DNarsGraph = null

	@Before
	def setUp(): Unit = {
		graph = DNarsGraphFactory.create(TEST_KEYSPACE, null)
	}

	@After
	def tearDown(): Unit = {
		graph.shutdown
		graph.clear
		graph = null
	}

	def createAndAdd(graph: DNarsGraph, statements: String*): List[Statement] = {
		val res = new ArrayBuffer[Statement]()
		for (str <- statements) {
			val stat = StatementParser(str)
			graph.statements.add(stat)
			res += stat
		}
		res.toList
	}

	def create(statements: String*): List[Statement] = {
		val res = new ListBuffer[Statement]()
		for (str <- statements)
			res += StatementParser(str)
		res.toList
	}

	def assertSeq(expected: Seq[Statement], actual: Seq[Statement]): Unit = {
		assertEquals(expected.size, actual.size)
		for (ste <- expected) {
			var found = false
			for (form <- ste.allForms)
				// cannot use "contains" because of Truth
				for (sta <- actual)
					if (form.equivalent(sta))
						found = true
			assertTrue("Statement " + ste + " not found.", found)
		}
	}

	def assertGraph(graph: DNarsGraph, expected: Seq[Statement]): Unit = {
		try {
			val actual = graph.statements.getAll
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