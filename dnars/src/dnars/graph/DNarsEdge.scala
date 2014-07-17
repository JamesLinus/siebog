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

package dnars.graph

import com.tinkerpop.gremlin.scala.ScalaEdge
import com.tinkerpop.blueprints.Edge
import dnars.base.Truth

/**
 * Wrapper around the ScalaEdge class. Inspired by 
 * <a href="https://github.com/mpollmeier/gremlin-scala">gremlin/scala project</a>.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class DNarsEdge(override val edge: Edge) extends ScalaEdge(edge) {
	def copula: String = getLabel
	def truth: Truth = getProperty[Truth]("truth")
	def truth_=(value: Truth): Unit = setProperty("truth", value)
}

object DNarsEdge {
	def apply(edge: Edge) = wrap(edge)
	implicit def wrap(edge: Edge) = new DNarsEdge(edge)
	implicit def unwrap(wrapper: DNarsEdge): Edge = wrapper.edge
}