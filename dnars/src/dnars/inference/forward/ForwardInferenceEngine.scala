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

package dnars.inference.forward

import dnars.base.Statement
import dnars.graph.DNarsGraph

/**
 * Syllogistic forward inference rules. The following table represents the summary
 * of supported premises and conclusions (up to NAL-5).
 *
 * <pre><code>
 *       | S->M                | S<->M     | M->S
 * ------|---------------------|-----------|--------------------
 * M->P  | S->P ded            | S->P ana  | S->P ind, S<->P cmp
 * -----------------------------------------------------------
 * M<->P | S->P ana'           | S<->P res | P->S ana'
 * -----------------------------------------------------------
 * P->M  | S->P abd, S<->P cmp | P->S ana  | --
 * </code></pre>
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class ForwardInferenceEngine(val graph: DNarsGraph) {
	val engines = List(
		new DeductionAnalogy(graph),
		new AnalogyResemblance(graph),
		new AbductionComparisonAnalogy(graph),
		new InductionComparison(graph),
		new AnalogyInv(graph))

	/**
	 * Returns a list of conclusions for the given set of input statements.
	 */
	def conclusions(input: Array[Statement]): Array[Statement] = {
		val inputList = input.toList // for compatibility with Java
		val result = engines.flatMap { engine => engine.apply(inputList) }
		result.toArray
	}

	def conclusions(input: Statement): Array[Statement] =
		conclusions(Array(input))

	/**
	 * Includes the given set of statements in the graph, applying forward inference rules along the way.
	 */
	def include(input: Array[Statement]): Unit = {
		val concl = conclusions(input)
		graph.add(input.toList ::: concl.toList)
	}
}