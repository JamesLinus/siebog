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

import siebog.dnars.DNarsTestBase
import siebog.dnars.base.Statement
import siebog.dnars.base.StatementParser
import siebog.dnars.inference.forward.AbductionComparisonAnalogy
import siebog.dnars.inference.forward.AnalogyResemblance
import siebog.dnars.inference.forward.DeductionAnalogy

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class ForwardInferenceTest extends DNarsTestBase {
	@Test
	def deductionAnalogy: Unit = {
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
		graph.statements.addAll(derived)

		val expected = List(
			invert(kb(3)),
			invert(kb(4)),
			StatementParser("tiger -> animal " + kb(0).truth.deduction(kb(2).truth)),
			StatementParser("feline -> animal " + kb(0).truth.analogy(kb(4).truth, false)),
			invert(kb(6)),
			StatementParser("(x einstein quantum) -> field " + kb(5).truth.analogy(kb(6).truth, false)))

		assertGraph(graph, kb.toList ::: expected)
	}

	@Test
	def analogyResemblance: Unit = {
		// M ~ P ::
		//		S -> M	=> S -> P ana'
		//		S ~ M	=> S ~ P res
		val stset = InferenceSets.getAnalogyResemblance
		graph.statements.addAll(stset.kb)

		val derived = new ListBuffer[Statement]()
		for (st <- stset.kb)
			derived ++= new AnalogyResemblance(graph).apply(st)
		graph.statements.addAll(derived)

		stset.assertGraph(graph)
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
		graph.statements.addAll(derived)

		val con1 = StatementParser("lion ~ tiger " + kb(0).truth.comparison(kb(3).truth))
		val con2 = StatementParser("tiger ~ lion " + kb(3).truth.comparison(kb(0).truth))
		val revised = StatementParser("lion ~ tiger " + con1.truth.revision(con2.truth))
		val expected = List(
			invert(kb(4)),
			StatementParser("lion -> tiger " + kb(0).truth.abduction(kb(3).truth)),
			revised,
			StatementParser("tiger -> feline " + kb(0).truth.analogy(kb(4).truth, false)),
			StatementParser("tiger -> lion " + kb(3).truth.abduction(kb(0).truth)),
			invert(revised),
			StatementParser("lion -> feline " + kb(3).truth.analogy(kb(4).truth, false)))

		assertGraph(graph, kb.toList ::: expected)
	}

	@Test
	def compoundExtentional: Unit = {
		val stset = InferenceSets.getCompoundExtentionalDeduction
		graph.statements.addAll(stset.kb)

		val derived = new ListBuffer[Statement]()
		for (st <- stset.kb)
			derived ++= new DeductionAnalogy(graph).apply(st)
		graph.statements.addAll(derived)

		stset.assertGraph(graph)
	}

	@Test
	def compoundTest: Unit = {
		val kb = createAndAdd(graph, "(x http://dbpedia.org/resource/Albert_Einstein http://dbpedia.org/resource/Physics) -> http://dbpedia.org/ontology/field (1.00,0.90)")
		val st = StatementParser("(x http://dbpedia.org/resource/Lise_Meitner http://dbpedia.org/resource/Physics) -> http://dbpedia.org/ontology/field (1.0, 0.9)")
		val derived = new AbductionComparisonAnalogy(graph).apply(st)
		val res = List(StatementParser("(x http://dbpedia.org/resource/Lise_Meitner http://dbpedia.org/resource/Physics) ~ (x http://dbpedia.org/resource/Albert_Einstein http://dbpedia.org/resource/Physics) (1.00,0.45)"))
		assertSeq(res, derived)
	}

	@Test
	def imageTest: Unit = {
		val kb = createAndAdd(graph, "tiger -> cat (1.0, 0.9)")
		val st = StatementParser("eat -> (x tiger bird) (1.0, 0.9)")
		val derived = new DeductionAnalogy(graph).apply(st)
		val res = List(StatementParser("eat -> (x cat bird) (1.0, 0.81)"))
		assertSeq(res, derived)
	}
}