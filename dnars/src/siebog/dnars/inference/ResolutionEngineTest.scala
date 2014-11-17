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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import siebog.dnars.DNarsTestUtils.TEST_KEYSPACE
import siebog.dnars.DNarsTestUtils.createAndAdd
import siebog.dnars.DNarsTestUtils.assertSeq
import siebog.dnars.base.AtomicTerm
import siebog.dnars.base.AtomicTerm.Question
import siebog.dnars.base.StatementParser
import siebog.dnars.base.Term
import siebog.dnars.graph.DNarsGraph
import siebog.dnars.graph.DNarsGraphFactory
import siebog.dnars.graph.StructuralTransform

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class ResolutionEngineTest {

	@Test
	def testAnswer(): Unit = {
		val graph = DNarsGraphFactory.create(TEST_KEYSPACE, null)
		try {
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
		} finally {
			graph.shutdown
			graph.clear
		}
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
		val a = ResolutionEngine.answer(graph, st)
		if (a.toList == List())
			assertEquals(answer, null)
		else
			assertSeq(List(StatementParser(answer)), a)
	}

	private def testInferenceSet(stset: InferenceSet): Unit = {
		val graph = DNarsGraphFactory.create(TEST_KEYSPACE, null)
		try {
			graph.statements.addAll(stset.kb)

			for (st <- stset.derived) {
				// expect the packed version, if available
				val expected = StructuralTransform.pack(st) match {
					case List(s1, _) => s1
					case _ => st
				}

				val answers = ResolutionEngine.answer(graph, st)
				assertNotEquals("No answer for " + st, List(), answers)
				val packed = answers.map(a => StructuralTransform.pack(a) match {
					case List(s1, _) => s1
					case _ => a
				})

				assertSeq(List(expected), packed)
			}
		} finally {
			graph.shutdown
			graph.clear
		}
	}
}