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

package siebog.dnars.graph

import org.junit.After
import org.junit.Before
import org.junit.Test
import siebog.dnars.DNarsTestUtils.TEST_KEYSPACE
import siebog.dnars.DNarsTestUtils.assertGraph
import siebog.dnars.DNarsTestUtils.create
import siebog.dnars.DNarsTestUtils.createAndAdd
import siebog.dnars.DNarsTestUtils.invert
import siebog.dnars.base.Statement
import siebog.dnars.base.Truth
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class DNarsGraphTest {
	var graph: DNarsGraph = null

	@Before
	def setUp(): Unit =
		graph = DNarsGraphFactory.create(TEST_KEYSPACE)

	@After
	def tearDown(): Unit = {
		graph.shutdown()
		graph.clear()
	}

	@Test
	def testAtomicAddition: Unit = {
		val st = createAndAdd(graph, // @formatter:off
			"cat -> animal (0.8, 0.77)",
			"cat ~ mammal (1.0, 0.9)",
			"tiger -> cat (1.0, 0.9)").toArray // @formatter:on

		// add the first statement again to test revision
		graph.add(st(0))

		val revisedTruth = st(0).truth.revision(st(0).truth)
		val revisedStatement = Statement(st(0).subj, st(0).copula, st(0).pred, revisedTruth)
		val expected = List(revisedStatement, st(1), invert(st(1)), st(2))
		assertGraph(graph, expected)
	}

	@Test
	def testPack() = {
		val kb = createAndAdd(graph,
			"cat -> (/ eat * bird) (1.0, 0.9)",
			"poo -> (/ eat dog *) (1.0, 0.9)",
			"(\\ dissolve * salt) -> liquid (1.0, 0.9)",
			"(\\ hate * cat) -> dog (1.0, 0.9)",
			"trout -> fish (1.0, 0.9)")

		val expected = create(
			"(x cat bird) -> eat (1.0, 0.9)",
			"(x dog poo) -> eat (1.0, 0.9)",
			"dissolve -> (x liquid salt) (1.0, 0.9)",
			"hate -> (x dog cat) (1.0, 0.9)",
			"trout -> fish (1.0, 0.9)")

		assertGraph(graph, expected)
	}

	@Test
	def testPackWithRevision(): Unit = {
		val kb = createAndAdd(graph,
			"(cat x bird) -> eat (0.66, 0.93)",
			"dissolve -> (x water salt) (0.73, 0.52)",
			"cat -> (/ eat * bird) (0.14, 0.8)",
			"(\\ dissolve water *) -> salt (0.55, 0.91)")

		val expected = create(
			"(cat x bird) -> eat " + kb(0).truth.revision(kb(2).truth),
			"dissolve -> (water x salt) " + kb(1).truth.revision(kb(3).truth))

		assertGraph(graph, expected)
	}
}