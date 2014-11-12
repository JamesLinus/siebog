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
			val stset = InferenceSets.getDeductionAnalogy
			graph.statements.addAll(stset.kb)

			val derived = new ListBuffer[Statement]()
			for (st <- stset.kb)
				derived ++= ForwardInference.deduction_analogy(graph, st)
			graph.statements.addAll(derived)

			stset.assertGraph(graph)
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
			val stset = InferenceSets.getAnalogyResemblance
			graph.statements.addAll(stset.kb)

			val derived = new ListBuffer[Statement]()
			for (st <- stset.kb)
				derived ++= ForwardInference.analogy_resemblance(graph, st)
			graph.statements.addAll(derived)

			stset.assertGraph(graph)
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
			val stset = InferenceSets.getAbductionComparisonAnalogy
			graph.statements.addAll(stset.kb)

			val derived = new ListBuffer[Statement]()
			for (st <- stset.kb)
				derived ++= ForwardInference.abduction_comparison_analogy(graph, st)
			graph.statements.addAll(derived)

			stset.assertGraph(graph)
		} finally {
			graph.shutdown
			graph.clear
		}
	}

	@Test
	def compoundExtentional: Unit = {
		val graph = DNarsGraphFactory.create(TEST_KEYSPACE, null)
		try {
			val stset = InferenceSets.getCompoundExtentionalDeduction
			graph.statements.addAll(stset.kb)

			val derived = new ListBuffer[Statement]()
			for (st <- stset.kb)
				derived ++= ForwardInference.deduction_analogy(graph, st)
			graph.statements.addAll(derived)

			stset.assertGraph(graph)
		} finally {
			graph.shutdown
			graph.clear
		}
	}
}