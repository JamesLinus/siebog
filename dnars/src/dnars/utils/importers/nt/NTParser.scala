package dnars.utils.importers.nt

import scala.util.parsing.combinator.RegexParsers
import dnars.base.Statement
import dnars.base.StatementParser

/**
 * N-Triplet parser. EBNF taken from http://www.w3.org/2001/sw/RDFCore/ntriples/
 * 
 * ntripleDoc	::=	line*	 
 * line			::=	ws* (comment | triple) ? eoln	 
 * comment		::=	'#' (character - ( cr | lf ) )*	 
 * triple		::=	subject ws+ predicate ws+ object ws* '.' ws*	 
 * subject		::=	uriref | namedNode	 
 * predicate	::=	uriref	 
 * object		::=	uriref | namedNode | literal	 
 * uriref		::=	'<' absoluteURI '>'	 
 * namedNode	::=	'_:' name	 
 * literal		::=	'"' string '"'	 
 * ws			::=	space | tab	 
 * eoln			::=	cr | lf | cr lf	 
 * space		::=	#x20 /* US-ASCII space - decimal 32 */	 
 * cr			::=	#xD /* US-ASCII carriage return - decimal 13 */	 
 * lf			::=	#xA /* US-ASCII linefeed - decimal 10 */	 
 * tab			::=	#x9 /* US-ASCII horizontal tab - decimal 9 */	 
 * string		::=	character* with escapes. Defined in section Strings	 
 * name			::=	[A-Za-z][A-Za-z0-9]*	 
 * absoluteURI	::=	( character - ( '<' | '>' | space ) )+	 
 * character	::=	[#x20-#x7E] /* US-ASCII space to decimal 127 */	
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
object NTLineParser extends RegexParsers {
	
	def line: Parser[List[String]] = (comment | triple) ^^ {
		case comment: String => List()
		case list: List[String] => list
	}
	
	def comment: Parser[String] = """#.*""".r ^^ { _.toString }
	
	def triple: Parser[List[String]] = subject ~ predicate ~ obj ~ "." ^^ {
		case subj ~ pred ~ obj ~ _ =>
			List(subj, pred, obj)
	} 
	
	def subject: Parser[String] = (uriRef | namedNode)
	def predicate: Parser[String] = uriRef
	def obj: Parser[String] = (uriRef | namedNode | lit)
	
	def uriRef: Parser[String] = s"""<[[$charMin-$charMax]&&[^<> ]]+>""".r ^^ {
		str => str.substring(1, str.length - 1)
	}

	def namedNode = s"""_:$name""".r ^^ { _.substring(2) }
	
	def lit = s""""[\\w -]*"@[a-z]{2}""".r  ^^ { str => str.substring(1, str.length - 1) }
	
	def apply(input: String): Option[Statement] = parseAll(line, input) match {
		case Success(result, _) => result match {
			case List() =>
				None
			case List(subj, pred, obj) =>
				None
		} 
		case failure: NoSuccess =>
			println(s"Failed to parse line [$input]")
			scala.sys.error(failure.msg)
	}
	
	val charMin = "\u0020"
	val charMax = "\u007E"
	val name = "[A-Za-z][A-Za-z0-9 ]*"
}