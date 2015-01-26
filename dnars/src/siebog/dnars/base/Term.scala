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

package siebog.dnars.base

import java.security.MessageDigest
import com.thinkaurelius.titan.diskstorage.ScanBuffer
import com.thinkaurelius.titan.diskstorage.WriteBuffer
import com.thinkaurelius.titan.graphdb.database.serialize.attribute.StringSerializer

/**
 * Base class for terms.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
abstract class Term extends Comparable[Term] {
	val id: String

	override def compareTo(other: Term): Int = id.compareTo(other.id)

	def complexity: Int

	override def toString() = id
}

/**
 * Atomic term with a string content.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
case class AtomicTerm(override val id: String) extends Term {
	override def complexity = 1
}

/**
 * Special atomic terms.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
object AtomicTerm {
	val Question = AtomicTerm("?")
	val Placeholder = AtomicTerm("*")
}

/**
 * Connectors for compound terms.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
object Connector {
	val Product = "x"
	val ExtImage = "/"
	val IntImage = "\\"
}

/**
 * Compound term with a connector and a list of terms.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
case class CompoundTerm(val con: String, val comps: List[AtomicTerm]) extends Term {
	override val id = comps.mkString(s"($con ", " ", ")")

	override def complexity = 1 + comps.foldLeft(0)(_ + _.complexity)
}