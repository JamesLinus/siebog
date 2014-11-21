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

import scala.collection.mutable.ListBuffer
import org.junit.Test
import siebog.dnars.DNarsTestUtils.TEST_KEYSPACE
import siebog.dnars.DNarsTestUtils.assertSeq
import siebog.dnars.DNarsTestUtils.createAndAdd
import siebog.dnars.DNarsTestUtils.invert
import siebog.dnars.base.Statement
import siebog.dnars.base.StatementParser
import siebog.dnars.graph.DNarsGraphFactory
import org.junit.Before
import org.junit.After
import siebog.dnars.graph.DNarsGraph

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class ForwardInferenceTest {
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

	@Test
	def deduction_analogy: Unit = {
		// M -> P  
		//		S -> M	=> S -> P ded 
		//		S ~ M	=> S -> P ana
		val stset = InferenceSets.getDeductionAnalogy
		graph.statements.addAll(stset.kb)

		val derived = new ListBuffer[Statement]()
		for (st <- stset.kb)
			derived ++= ForwardInference.deduction_analogy(graph, st)
		graph.statements.addAll(derived)

		stset.assertGraph(graph)
	}

	@Test
	def analogy_resemblance: Unit = {
		// M ~ P ::
		//		S -> M	=> S -> P ana'
		//		S ~ M	=> S ~ P res
		val stset = InferenceSets.getAnalogyResemblance
		graph.statements.addAll(stset.kb)

		val derived = new ListBuffer[Statement]()
		for (st <- stset.kb)
			derived ++= ForwardInference.analogy_resemblance(graph, st)
		graph.statements.addAll(derived)

		stset.assertGraph(graph)
	}

	@Test
	def abduction_comparison_analogy: Unit = {
		// P -> M 
		//		S -> M	=> S -> P abd, S ~ P cmp
		//		S ~ M 	=> P -> S ana
		val stset = InferenceSets.getAbductionComparisonAnalogy
		graph.statements.addAll(stset.kb)

		val derived = new ListBuffer[Statement]()
		for (st <- stset.kb)
			derived ++= ForwardInference.abduction_comparison_analogy(graph, st)
		graph.statements.addAll(derived)

		stset.assertGraph(graph)
	}

	@Test
	def compoundExtentional: Unit = {
		val stset = InferenceSets.getCompoundExtentionalDeduction
		graph.statements.addAll(stset.kb)

		val derived = new ListBuffer[Statement]()
		for (st <- stset.kb)
			derived ++= ForwardInference.deduction_analogy(graph, st)
		graph.statements.addAll(derived)

		stset.assertGraph(graph)
	}

	@Test
	def compoundTest: Unit = {
		val kb = createAndAdd(graph, "(x http://dbpedia.org/resource/Albert_Einstein http://dbpedia.org/resource/Physics) -> http://dbpedia.org/ontology/field (1.00,0.90)")
		val st = StatementParser("(x http://dbpedia.org/resource/Lise_Meitner http://dbpedia.org/resource/Physics) -> http://dbpedia.org/ontology/field (1.0, 0.9)")
		val derived = ForwardInference.abduction_comparison_analogy(graph, st)
		val res = List(StatementParser("(x http://dbpedia.org/resource/Lise_Meitner http://dbpedia.org/resource/Physics) ~ (x http://dbpedia.org/resource/Albert_Einstein http://dbpedia.org/resource/Physics) (1.00,0.45)"))
		assertSeq(res, derived)
	}

	@Test
	def imageTest: Unit = {
		val kb = createAndAdd(graph, "tiger -> cat (1.0, 0.9)")
		val st = StatementParser("(x cat bird) -> eats (1.0, 0.9)")
		val derived = ForwardInference.deduction_analogy(graph, st)
		val res = List(StatementParser("(x tiger bird) -> eats (1.0, 0.9)"))
		assertSeq(res, derived)
	}
}