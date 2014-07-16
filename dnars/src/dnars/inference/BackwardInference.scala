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

package dnars.inference

import dnars.gremlin.DNarsGraph
import dnars.base.Statement
import dnars.gremlin.DNarsVertex

/**
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
object BackwardInference {
	def findPath(graph: DNarsGraph, st: Statement): Boolean = {
		// both terms should exist
		graph.getV(st.subj) match {
			case None => false
			case Some(s) =>
				val subj: DNarsVertex = s
				graph.getV(st.pred) match {
					case None => false
					case Some(pred) =>
						subj.startPipe.as("x").outE.inV.loop("x", { lp => lp.getObject != pred }).hasNext
				}
		}
	}
}