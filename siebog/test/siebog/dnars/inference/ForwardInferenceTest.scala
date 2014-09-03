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

package siebog.dnars.inference

import org.junit.Test
import siebog.dnars.graph.DNarsGraphFactory
import siebog.dnars.TestUtils._
import siebog.dnars.base.StatementParser
import scala.collection.mutable.ListBuffer
import siebog.dnars.base.Statement

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class ForwardInferenceTest {
	@Test
	def deduction_analogy: Unit = {
		// M -> P  
		//		S -> M	=> S -> P ded 
		//		S ~ M	=> S -> P ana
		val graph = DNarsGraphFactory.create(TEST_KEYSPACE, null)
		try {
			val kb = createAndAdd(graph,
				"cat -> animal (0.82, 0.91)",
				"water -> liquid (1.0, 0.9)",
				"tiger -> cat (0.5, 0.7)",
				"developer ~ job (1.0, 0.9)",
				"feline ~ cat (0.76, 0.83)")
			val derived = new ListBuffer[Statement]()
			for (st <- kb)
				derived ++= ForwardInference.deduction_analogy(graph, st)
			graph.statements.addAll(derived)
			val res = List(
				invert(kb(3)),
				invert(kb(4)),
				StatementParser("tiger -> animal " + kb(0).truth.deduction(kb(2).truth)),
				StatementParser("feline -> animal " + kb(0).truth.analogy(kb(4).truth, false)))
			assertGraph(graph, kb.toList, res)
		} finally {
			graph.shutdown
			graph.clear
		}
	}

	@Test
	def analogy_resemblance: Unit = {
		// M ~ P ::
		//		S -> M	=> S -> P ana'
		//		S ~ M	=> S ~ P res
		val graph = DNarsGraphFactory.create(TEST_KEYSPACE, null)
		try {
			val kb = createAndAdd(graph,
				"developer -> job (0.6, 0.77)",
				"cat ~ feline (0.33, 0.51)",
				"tiger -> cat (0.95, 0.83)",
				"water -> liquid (0.63, 0.72)",
				"lion ~ cat (0.85, 0.48)")
			val derived = new ListBuffer[Statement]()
			for (st <- kb)
				derived ++= ForwardInference.analogy_resemblance(graph, st)
			graph.statements.addAll(derived)
			val st = StatementParser("lion ~ feline " + kb(1).truth.resemblance(kb(4).truth))
			val res = List(
				invert(kb(1)),
				invert(kb(4)),
				StatementParser("tiger -> feline " + kb(1).truth.analogy(kb(2).truth, true)),
				StatementParser("tiger -> lion " + kb(4).truth.analogy(kb(2).truth, true)),
				st,
				invert(st))
			assertGraph(graph, kb, res)
		} finally {
			graph.shutdown
			graph.clear
		}
	}

	@Test
	def abduction_comparison_analogy: Unit = {
		// P -> M 
		//		S -> M	=> S -> P abd, S ~ P cmp
		//		S ~ M 	=> P -> S ana
		val graph = DNarsGraphFactory.create(TEST_KEYSPACE, null)
		try {
			val kb = createAndAdd(graph,
				"tiger -> cat (1.0, 0.9)",
				"water -> liquid (0.68, 0.39)",
				"developer -> job (0.93, 0.46)",
				"lion -> cat (0.43, 0.75)",
				"feline ~ cat (0.49, 0.52)")
			val derived = new ListBuffer[Statement]()
			for (st <- kb)
				derived ++= ForwardInference.abduction_comparison_analogy(graph, st)
			graph.statements.addAll(derived)
			val con1 = StatementParser("lion ~ tiger " + kb(0).truth.comparison(kb(3).truth))
			val con2 = StatementParser("tiger ~ lion " + kb(3).truth.comparison(kb(0).truth))
			val revised = StatementParser("lion ~ tiger " + con1.truth.revision(con2.truth))
			val res = List(
				invert(kb(4)),
				StatementParser("lion -> tiger " + kb(0).truth.abduction(kb(3).truth)),
				revised,
				StatementParser("tiger -> feline " + kb(0).truth.analogy(kb(4).truth, false)),
				StatementParser("tiger -> lion " + kb(3).truth.abduction(kb(0).truth)),
				invert(revised),
				StatementParser("lion -> feline " + kb(3).truth.analogy(kb(4).truth, false)))
			assertGraph(graph, kb, res)
		} finally {
			graph.shutdown
			graph.clear
		}
	}

	@Test
	def compoundExtentional: Unit = {
		val graph = DNarsGraphFactory.create(TEST_KEYSPACE, null)
		try {
			val kb = createAndAdd(graph,
				"(cat x bird) -> eat (1.0, 0.9)",
				"tiger -> cat (1.0, 0.9)")
			val derived = new ListBuffer[Statement]()
			for (st <- kb)
				derived ++= ForwardInference.deduction_analogy(graph, st)
			graph.statements.addAll(derived)

			val ded = kb(0).truth.deduction(kb(1).truth)
			val res = List(
				StatementParser("cat -> (/ eat * bird) (1.0, 0.9)"),
				StatementParser("bird -> (/ eat cat *) (1.0, 0.9)"),
				StatementParser("(tiger x bird) -> eat " + ded),
				StatementParser("tiger -> (/ eat * bird) " + ded),
				StatementParser("bird -> (/ eat tiger *) " + ded))
			assertGraph(graph, kb, res)
		} finally {
			graph.shutdown
			graph.clear
		}
	}
}