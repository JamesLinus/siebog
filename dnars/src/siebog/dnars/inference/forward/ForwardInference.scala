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

package siebog.dnars.inference.forward

import siebog.dnars.graph.DNarsGraph
import siebog.dnars.base.Statement
import com.tinkerpop.blueprints.Vertex
import siebog.dnars.graph.DNarsVertex

/**
 * Base class for forward inference. During the inference process,
 * the first statement is always taken from the graph, while the second
 * is taken from the input judgement(s).
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
abstract class ForwardInference(val graph: DNarsGraph) {
	def apply(judgement: Statement): List[Statement] =
		judgement.allImages.flatMap { j: Statement => doApply(j) }

	def apply(judgements: List[Statement]): List[Statement] =
		judgements.flatMap { j: Statement => apply(j) }

	protected def doApply(judgement: Statement): List[Statement]

	protected def keepIfValid(st: Statement): List[Statement] =
		if (graph.validStatement(st)) List(st) else List()
}