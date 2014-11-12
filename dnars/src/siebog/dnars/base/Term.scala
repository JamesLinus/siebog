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
import com.thinkaurelius.titan.core.AttributeSerializer
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
 * Used when an AtomicTerm is de-/serialized as a vertex attribute.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class AtomicTermSerializer extends AttributeSerializer[AtomicTerm] {
	val strser = new StringSerializer

	override def read(buffer: ScanBuffer): AtomicTerm =
		AtomicTerm(strser.read(buffer))

	override def writeObjectData(buffer: WriteBuffer, attribute: AtomicTerm): Unit =
		strser.writeObjectData(buffer, attribute.id)

	override def verifyAttribute(value: AtomicTerm): Unit = {}

	override def convert(value: Any): AtomicTerm = value match {
		case str: String => AtomicTerm(str)
		case _ => null
	}
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
case class CompoundTerm(val con: String, val comps: Seq[AtomicTerm]) extends Term {
	override val id = comps.mkString(s"($con ", " ", ")")

	override def complexity = 1 + comps.foldLeft(0)(_ + _.complexity)
}

/**
 * Used when a CompoundTerm is de-/serialized as a vertex attribute.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class CompoundTermSerializer extends AttributeSerializer[CompoundTerm] {
	val strser = new StringSerializer

	override def read(buffer: ScanBuffer): CompoundTerm =
		str2cterm(strser.read(buffer))

	override def writeObjectData(buffer: WriteBuffer, attribute: CompoundTerm): Unit =
		strser.writeObjectData(buffer, cterm2str(attribute))

	override def verifyAttribute(value: CompoundTerm): Unit = {}

	override def convert(value: Any): CompoundTerm = value match {
		case str: String => str2cterm(str)
		case _ => null
	}

	private def cterm2str(term: CompoundTerm): String =
		term.con + " " + term.comps.mkString("\t")

	private def str2cterm(str: String): CompoundTerm = {
		val elems = str.split(" ")
		val compsStr = elems(1).split("\t")
		val comps = for (c <- compsStr) yield AtomicTerm(c)
		CompoundTerm(elems(0), comps)
	}
}