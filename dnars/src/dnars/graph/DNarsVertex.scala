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

import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.gremlin.scala.ScalaVertex
import dnars.base.Truth
import dnars.base.Term

/**
 * Wrapper around the ScalaVertex class. Inspired by 
 * <a href="https://github.com/mpollmeier/gremlin-scala">gremlin/scala project</a>.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class DNarsVertex(override val vertex: Vertex) extends ScalaVertex(vertex) {
	def term: Term = getProperty[Term]("term")
	def term_=(value: Term): Unit = setProperty("term", value)
}

object DNarsVertex {
	def apply(vertex: Vertex) = wrap(vertex)
	implicit def wrap(vertex: Vertex) = new DNarsVertex(vertex)
	implicit def unwrap(wrapper: DNarsVertex): Vertex = wrapper.vertex
}