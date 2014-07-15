package dnars.base

import java.security.MessageDigest

/**
 * Base class for terms.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
abstract class Term {
	val id: String
	
	def complexity: Int
	
	override def toString() = id
}

/**
 * Atomic term with a string content.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
case class AtomicTerm(override val id: String) extends Term  {
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
	lazy override val id = comps.mkString(s"($con ", " ", ")")
	
	override def complexity = 1 + comps.foldLeft(0)(_ + _.complexity)
}