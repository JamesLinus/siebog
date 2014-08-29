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

import scala.collection.mutable.ListBuffer
import siebog.dnars.graph.DNarsGraphFactory;
import siebog.dnars.TestUtils.TEST_KEYSPACE
import siebog.dnars.TestUtils.assertGraph
import siebog.dnars.TestUtils.createAndAdd
import siebog.dnars.TestUtils.invert
import siebog.dnars.base.Statement
import siebog.dnars.base.StatementParser
import siebog.dnars.base.Truth
import org.junit.Test

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class DNarsGraphTest {
	@Test
	def testAtomicAddition: Unit = {
		val graph = DNarsGraphFactory.create(TEST_KEYSPACE, null)
		try {
			val st = createAndAdd(graph, // @formatter:off
				"cat -> animal (0.8, 0.77)",
				"cat ~ mammal (1.0, 0.9)",
				"tiger -> cat (1.0, 0.9)") // @formatter:on

			// add the first statement again to test revision
			graph.statements.add(st(0))
			val t = st(0).truth.revision(st(0).truth)
			st(0) = Statement(st(0).subj, st(0).copula, st(0).pred, t)

			assertGraph(graph, st, Seq(invert(st(1))))
		} finally {
			graph.shutdown(true)
		}
	}

	@Test
	def testCompoundAddition: Unit = {
		val graph = DNarsGraphFactory.create(TEST_KEYSPACE, null)
		try {
			val kb = createAndAdd(graph, // @formatter:off
				"(cat x bird) -> eat (0.66, 0.93)",
				"dissolve -> (water x salt) (0.73, 0.52)",
				"poo -> (/ eat dog *) (0.13, 0.44)",
				"(\\ hate * cat) -> dog (0.85, 0.9)") // @formatter:on

			// apply revision
			val t = new ListBuffer[Truth]()
			var i = 0
			for (kbStat <- kb) {
				graph.statements.add(kbStat)
				val revised = kbStat.truth.revision(kbStat.truth)
				kb(i) = Statement(kbStat.subj, kbStat.copula, kbStat.pred, revised)
				t += revised
				i += 1
			}

			val res = List( // @formatter:off
				StatementParser("cat -> (/ eat * bird) " + t(0)),
				StatementParser("bird -> (/ eat cat *) " + t(0)),
				StatementParser("(\\ dissolve * salt) -> water " + t(1)),
				StatementParser("(\\ dissolve water *) -> salt " + t(1)),
				StatementParser("(dog x poo) -> eat " + t(2)),
				StatementParser("dog -> (/ eat * poo) " + t(2)),
				StatementParser("hate -> (dog x cat) " + t(3)),
				StatementParser("(\\ hate dog *) -> cat " + t(3))) // @formatter:on
			assertGraph(graph, kb, res)
		} finally {
			graph.shutdown(true)
		}
	}

	@Test
	def testUnpack: Unit = {
		val graph = DNarsGraphFactory.create(TEST_KEYSPACE, null)
		try {
			val kb = createAndAdd(graph, // @formatter:off
				"(cat x bird) -> eat (1.0, 0.9)",
				"developer -> job (1.0, 0.9)",
				"dissolve -> (x water salt) (1.0, 0.9)")
			val res = List(
				StatementParser("cat -> (/ eat * bird) (1.0, 0.9)"),
				StatementParser("bird -> (/ eat cat *) (1.0, 0.9)"),
				StatementParser("(\\ dissolve * salt) -> water (1.0, 0.9)"),
				StatementParser("(\\ dissolve water *) -> salt (1.0, 0.9)")) // @formatter:on
			assertGraph(graph, kb, res)
		} finally {
			graph.shutdown(true)
		}
	}

	@Test
	def testPack = {
		val graph = DNarsGraphFactory.create(TEST_KEYSPACE, null)
		try {
			val kb = createAndAdd(graph, // @formatter:off
				"cat -> (/ eat * bird) (1.0, 0.9)",
				"poo -> (/ eat dog *) (1.0, 0.9)",
				"(\\ dissolve * salt) -> liquid (1.0, 0.9)",
				"(\\ hate * cat) -> dog (1.0, 0.9)")
			val res = List(
				StatementParser("(x cat bird) -> eat (1.0, 0.9)"),
				StatementParser("bird -> (/ eat cat *) (1.0, 0.9)"),

				StatementParser("(x dog poo) -> eat (1.0, 0.9)"),
				StatementParser("dog -> (/ eat * poo) (1.0, 0.9)"),

				StatementParser("dissolve -> (x liquid salt) (1.0, 0.9)"),
				StatementParser("(\\ dissolve liquid *) -> salt (1.0, 0.9)"),

				StatementParser("hate -> (x dog cat) (1.0, 0.9)"),
				StatementParser("(\\ hate dog *) -> cat (1.0, 0.9)")) // @formatter:on
			assertGraph(graph, kb, res)
		} finally {
			graph.shutdown(true)
		}
	}
}