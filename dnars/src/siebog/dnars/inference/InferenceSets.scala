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

package siebog.dnars.inference

import siebog.dnars.base.Statement
import siebog.dnars.DNarsTestUtils.create
import siebog.dnars.DNarsTestUtils.invert
import siebog.dnars.DNarsTestUtils.assertSeq
import siebog.dnars.base.StatementParser
import siebog.dnars.graph.DNarsGraph

class InferenceSet(val kb: List[Statement], val derived: List[Statement]) {
	def assertGraph(graph: DNarsGraph): Unit = {
		val expected = kb ::: derived
		val actual = graph.statements.getAll
		assertSeq(expected, actual)
	}
}

object InferenceSets {
	def getDeductionAnalogy: InferenceSet = {
		val kb = create("cat -> animal (0.82, 0.91)",
			"water -> liquid (1.0, 0.9)",
			"tiger -> cat (0.5, 0.7)",
			"developer ~ job (1.0, 0.9)",
			"feline ~ cat (0.76, 0.83)")
		val derived = List(
			invert(kb(3)),
			invert(kb(4)),
			StatementParser("tiger -> animal " + kb(0).truth.deduction(kb(2).truth)),
			StatementParser("feline -> animal " + kb(0).truth.analogy(kb(4).truth, false)))
		new InferenceSet(kb, derived)
	}

	def getAnalogyResemblance: InferenceSet = {
		val kb = create("developer -> job (0.6, 0.77)",
			"cat ~ feline (0.33, 0.51)",
			"tiger -> cat (0.95, 0.83)",
			"water -> liquid (0.63, 0.72)",
			"lion ~ cat (0.85, 0.48)")
		val st = StatementParser("lion ~ feline " + kb(1).truth.resemblance(kb(4).truth))
		val derived = List(
			invert(kb(1)),
			invert(kb(4)),
			StatementParser("tiger -> feline " + kb(1).truth.analogy(kb(2).truth, true)),
			StatementParser("tiger -> lion " + kb(4).truth.analogy(kb(2).truth, true)),
			st,
			invert(st))
		new InferenceSet(kb, derived)
	}

	def getAbductionComparisonAnalogy: InferenceSet = {
		val kb = create("tiger -> cat (1.0, 0.9)",
			"water -> liquid (0.68, 0.39)",
			"developer -> job (0.93, 0.46)",
			"lion -> cat (0.43, 0.75)",
			"feline ~ cat (0.49, 0.52)")
		val con1 = StatementParser("lion ~ tiger " + kb(0).truth.comparison(kb(3).truth))
		val con2 = StatementParser("tiger ~ lion " + kb(3).truth.comparison(kb(0).truth))
		val revised = StatementParser("lion ~ tiger " + con1.truth.revision(con2.truth))
		val derived = List(
			invert(kb(4)),
			StatementParser("lion -> tiger " + kb(0).truth.abduction(kb(3).truth)),
			revised,
			StatementParser("tiger -> feline " + kb(0).truth.analogy(kb(4).truth, false)),
			StatementParser("tiger -> lion " + kb(3).truth.abduction(kb(0).truth)),
			invert(revised),
			StatementParser("lion -> feline " + kb(3).truth.analogy(kb(4).truth, false)))
		new InferenceSet(kb, derived)
	}

	def getCompoundExtentionalDeduction: InferenceSet = {
		val kb = create("(cat x bird) -> eat (1.0, 0.9)",
			"tiger -> cat (1.0, 0.9)")
		val ded = kb(0).truth.deduction(kb(1).truth)
		val derived = List(
			StatementParser("cat -> (/ eat * bird) (1.0, 0.9)"),
			StatementParser("bird -> (/ eat cat *) (1.0, 0.9)"),
			StatementParser("(tiger x bird) -> eat " + ded),
			StatementParser("tiger -> (/ eat * bird) " + ded),
			StatementParser("bird -> (/ eat tiger *) " + ded))
		new InferenceSet(kb, derived)
	}

	def getAllSets: List[InferenceSet] =
		List(getDeductionAnalogy, getAnalogyResemblance, getAbductionComparisonAnalogy, getCompoundExtentionalDeduction)
}