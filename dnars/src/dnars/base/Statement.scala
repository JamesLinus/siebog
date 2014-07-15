package dnars.base

object Copula {
	val Inherit: String = "->"
	val Similar: String = "~"
}

/**
 * Definition of a statement: subject, copula, predicate, frequency, and confidence.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
case class Statement(val subj: Term, val copula: String, val pred: Term, val truth: Truth) {
	lazy val id = s"$subj $copula $pred $truth"
	
	def equivalent(other: Statement): Boolean = 
		subj == other.subj && copula == other.copula && pred == other.pred && truth.closeTo(other.truth)
	
	override def toString = id
}