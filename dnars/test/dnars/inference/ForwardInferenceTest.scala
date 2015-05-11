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

package dnars.inference

import org.junit.After
import org.junit.Before
import org.junit.Test

import dnars.DNarsTestUtils.TEST_KEYSPACE
import dnars.DNarsTestUtils.assertSeq
import dnars.DNarsTestUtils.create
import dnars.DNarsTestUtils.createAndAdd
import dnars.base.StatementParser
import dnars.graph.DNarsGraph
import dnars.graph.DNarsGraphFactory
import dnars.inference.forward.AbductionComparisonAnalogy
import dnars.inference.forward.AnalogyResemblance
import dnars.inference.forward.DeductionAnalogy

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class ForwardInferenceTest {
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
	def deductionAnalogy(): Unit = {
		// M -> P  ::
		//		S -> M	=> S -> P ded 
		//		S ~ M	=> S -> P ana
		val kb = createAndAdd(graph,
			"cat -> animal (0.82, 0.91)",
			"water -> liquid (1.0, 0.9)",
			"tiger -> cat (0.5, 0.7)",
			"developer ~ job (1.0, 0.9)",
			"feline ~ cat (0.76, 0.83)",
			"(x einstein physics) -> field (1.0, 0.9)",
			"quantum ~ physics (1.0, 0.9)")

		val derived = new DeductionAnalogy(graph).apply(kb)
		val expected = create(
			"tiger -> animal " + kb(0).truth.deduction(kb(2).truth),
			"feline -> animal " + kb(0).truth.analogy(kb(4).truth, false),
			"(x einstein quantum) -> field " + kb(5).truth.analogy(kb(6).truth, false))

		assertSeq(expected, derived)
	}

	@Test
	def analogyResemblance: Unit = {
		// M ~ P ::
		//		S -> M	=> S -> P ana'
		//		S ~ M	=> S ~ P res
		val kb = createAndAdd(graph,
			"developer -> job (0.6, 0.77)",
			"cat ~ feline (0.33, 0.51)",
			"tiger -> cat (0.95, 0.83)",
			"water -> liquid (0.63, 0.72)",
			"lion ~ cat (0.85, 0.48)")

		val derived = new AnalogyResemblance(graph).apply(kb)
		val expected = create(
			"tiger -> feline " + kb(1).truth.analogy(kb(2).truth, true),
			"tiger -> lion " + kb(4).truth.analogy(kb(2).truth, true),
			"lion ~ feline " + kb(1).truth.resemblance(kb(4).truth))

		assertSeq(expected, derived)
	}

	@Test
	def abductionComparisonAnalogy: Unit = {
		// P -> M ::
		//		S -> M	=> S -> P abd, S ~ P cmp
		//		S ~ M 	=> P -> S ana
		val kb = createAndAdd(graph,
			"tiger -> cat (1.0, 0.9)",
			"water -> liquid (0.68, 0.39)",
			"developer -> job (0.93, 0.46)",
			"lion -> cat (0.43, 0.75)",
			"feline ~ cat (0.49, 0.52)")

		val derived = new AbductionComparisonAnalogy(graph).apply(kb)

		val expected = create(
			"lion -> tiger " + kb(0).truth.abduction(kb(3).truth),
			"lion ~ tiger " + kb(0).truth.comparison(kb(3).truth),
			"tiger ~ lion " + kb(0).truth.comparison(kb(3).truth),
			"tiger -> feline " + kb(0).truth.analogy(kb(4).truth, false),
			"tiger -> lion " + kb(3).truth.abduction(kb(0).truth),
			"lion -> feline " + kb(3).truth.analogy(kb(4).truth, false))

		assertSeq(expected, derived)
	}

	@Test
	def compoundExtentional: Unit = {
		val kb = createAndAdd(graph,
			"(cat x bird) -> eat (1.0, 0.9)",
			"tiger -> cat (1.0, 0.9)")

		val derived = new DeductionAnalogy(graph).apply(kb)
		val expected = create(
			"(tiger x bird) -> eat " + kb(0).truth.deduction(kb(1).truth))

		assertSeq(expected, derived)
	}

	@Test
	def compoundTest: Unit = {
		val kb = createAndAdd(graph,
			"(x http://dbpedia.org/resource/Albert_Einstein http://dbpedia.org/resource/Physics) -> http://dbpedia.org/ontology/field (0.84, 0.77)")

		val st = StatementParser("(x http://dbpedia.org/resource/Lise_Meitner http://dbpedia.org/resource/Physics) -> http://dbpedia.org/ontology/field (0.94, 0.63)")
		val derived = new AbductionComparisonAnalogy(graph).apply(st)
		val expected = create(
			"(x http://dbpedia.org/resource/Lise_Meitner http://dbpedia.org/resource/Physics) ~ (x http://dbpedia.org/resource/Albert_Einstein http://dbpedia.org/resource/Physics) " + kb(0).truth.comparison(st.truth),
			"http://dbpedia.org/resource/Lise_Meitner -> http://dbpedia.org/resource/Albert_Einstein " + kb(0).truth.abduction(st.truth),
			"http://dbpedia.org/resource/Lise_Meitner ~ http://dbpedia.org/resource/Albert_Einstein " + kb(0).truth.comparison(st.truth))

		assertSeq(expected, derived)
	}

	@Test
	def imageTest: Unit = {
		val kb = createAndAdd(graph, "tiger -> cat (1.0, 0.9)")
		val st = StatementParser("eat -> (x tiger bird) (1.0, 0.9)")
		val derived = new DeductionAnalogy(graph).apply(st)
		val expected = create("eat -> (x cat bird) (1.0, 0.81)")
		assertSeq(expected, derived)
	}
}