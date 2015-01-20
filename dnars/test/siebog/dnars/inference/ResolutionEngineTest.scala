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

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

import siebog.dnars.DNarsTestUtils.TEST_KEYSPACE
import siebog.dnars.DNarsTestUtils.assertSeq
import siebog.dnars.DNarsTestUtils.createAndAdd
import siebog.dnars.base.StatementParser
import siebog.dnars.graph.DNarsGraph
import siebog.dnars.graph.DNarsGraphFactory

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class ResolutionEngineTest {
	var graph: DNarsGraph = null

	@Before
	def setUp(): Unit =
		graph = DNarsGraphFactory.create(TEST_KEYSPACE)

	@After
	def tearDown: Unit = {
		graph.shutdown
		graph.clear
		graph = null
	}

	@Test
	def testAnswer(): Unit = {
		createAndAdd(graph,
			"cat -> animal (1.0, 0.9)",
			"developer ~ job (0.87, 0.91)")

		assertAnswer(graph, "? -> cat", null)
		assertAnswer(graph, "? -> animal", "cat -> animal (1.0, 0.9)")
		assertAnswer(graph, "cat -> ?", "cat -> animal (1.0, 0.9)")
		assertAnswer(graph, "animal -> ?", null)
		assertAnswer(graph, "water -> ?", null)
		assertAnswer(graph, "developer ~ ?", "developer ~ job (0.87, 0.91)")
		assertAnswer(graph, "? ~ developer", "job ~ developer (0.87, 0.91)")
	}

	@Test
	def testMultipleAnswers(): Unit = {
		val kb = createAndAdd(graph,
			"cat -> animal (0.6, 0.3)",
			"bird -> animal (1.0, 0.9)",
			"developer ~ job (0.87, 0.91)",
			"tiger -> animal (0.6, 0.4)")

		val q = StatementParser("? -> animal (1.0, 0.9)")
		val answers = ResolutionEngine.answer(graph, q, 2)
		assertSeq(List(kb(1), kb(3)), answers)
	}

	@Test
	def testBackwardDeductionAnalogy(): Unit =
		testInferenceSet(InferenceSets.getDeductionAnalogy)

	@Test
	def testBackwardAbductionComparisonAnalogy(): Unit =
		testInferenceSet(InferenceSets.getAbductionComparisonAnalogy)

	@Test
	def testBackwardAnalogyResemblance(): Unit =
		testInferenceSet(InferenceSets.getAnalogyResemblance)

	@Test
	def testBackwardCompoundExtentionalDeduction(): Unit =
		testInferenceSet(InferenceSets.getCompoundExtentionalDeduction)

	private def assertAnswer(graph: DNarsGraph, question: String, answer: String): Unit = {
		val st = StatementParser(question)
		val a = ResolutionEngine.answer(graph, st, 1)
		if (a.toList == List())
			assertEquals(answer, null)
		else
			assertSeq(List(StatementParser(answer)), a)
	}

	private def testInferenceSet(stset: InferenceSet): Unit = {
		graph.add(stset.kb)

		for (st <- stset.derived) {
			// expect the packed version, if available
			val expected = st.pack match {
				case List(s1, _) => s1
				case _ => st
			}

			val answers = ResolutionEngine.answer(graph, st)
			assertNotEquals("No answer for " + st, List(), answers)
			val packed = answers.map(a => a.pack match {
				case List(s1, _) => s1
				case _ => a
			})

			assertSeq(List(expected), packed)
		}
	}
}