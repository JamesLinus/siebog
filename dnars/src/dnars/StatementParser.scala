package dnars

import scala.util.parsing.combinator.RegexParsers
import dnars.base.{ AtomicTerm, CompoundTerm }
import dnars.base.{ Statement, Term }
import dnars.base.Connector.Product
import dnars.base.Copula._
import dnars.base.Copula
import dnars.base.Truth
import dnars.base.Truth
import dnars.base.Truth
import dnars.base.Connector._

/**
 * Creates Statement objects from input strings.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
object StatementParser extends RegexParsers {
	def number: Parser[Double] = """\d+(\.\d*)?""".r ^^ { _.toDouble }
	
	def atomicTerm: Parser[AtomicTerm] = """\w+""".r ^^ { AtomicTerm(_) }
	
	def connector: Parser[String] = (Product | ExtImage | IntImage)
	
	def compoundTerm: Parser[CompoundTerm] = (prefixCompTerm | infixCompTerm)
	def prefixCompTerm: Parser[CompoundTerm] = "(" ~ connector ~ atomicTerm ~ rep1(atomicTerm) ~ ")" ^^ {
		case "(" ~ con ~ t ~ list ~ ")" => CompoundTerm(con, t :: list)
	}
	def infixCompTerm: Parser[CompoundTerm] = "(" ~ atomicTerm ~ rep1(connector ~ atomicTerm) ~ ")" ^^ {
		case "(" ~ t ~ list ~ ")" => CompoundTerm(list.head._1, t :: (for (x <- list) yield x._2))
	}
	
	def term: Parser[Term] = (atomicTerm | compoundTerm)
	
	def copula: Parser[String] = (Inherit | Similar)
	
	def statement: Parser[Statement] = (judgement | question)
	
	def judgement: Parser[Statement] = term ~ copula ~ term ~ "(" ~ number ~ "," ~ number ~ ")" ^^ {
		case s ~ r ~ p ~ "(" ~ f ~ "," ~ c ~ ")" =>
			new Statement(s, r.toString, p, Truth(f, c))
	}
	
	def question: Parser[Statement] = ("?" ~ copula ~ term | term ~ copula ~ "?") ^^ {
		case "?" ~ r ~ p =>
			new Statement(AtomicTerm.Question, r.toString, p.asInstanceOf[Term], Truth(1.0, 0.9))
		case s ~ r ~ "?" =>
			new Statement(s.asInstanceOf[Term], r.toString, AtomicTerm.Question, Truth(1.0, 0.9))
	}
	
	def apply(input: String): Statement = parseAll(statement, input) match {
		case Success(result, _) => result
		case failure: NoSuccess => 
			println(s"Failed to parse statement [$input]")
			scala.sys.error(failure.msg)
	}
}