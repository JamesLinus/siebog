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

import scala.util.parsing.combinator.RegexParsers

import siebog.dnars.base.AtomicTerm.Placeholder
import siebog.dnars.base.Connector.ExtImage
import siebog.dnars.base.Connector.IntImage
import siebog.dnars.base.Connector.Product
import siebog.dnars.base.Copula.Inherit
import siebog.dnars.base.Copula.Similar

/**
 * Creates Statement objects from input strings.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
object StatementParser extends RegexParsers {
	def number: Parser[Double] = """\d+(\.\d*)?""".r ^^ { _.toDouble }
	
	//[\p{L}0-9[\p{Punct}&&[^()]]–…—]
	def atomicTerm: Parser[AtomicTerm] = """[^\s()]+""".r ^^ { AtomicTerm(_) }
	
	def image: Parser[String] = (ExtImage | IntImage)
	def connector: Parser[String] = (Product | image)
	
	def compoundTerm: Parser[CompoundTerm] = (prefixCompTerm | infixCompTerm | imgCompTerm1 | imgCompTerm2)
	def prefixCompTerm: Parser[CompoundTerm] = "(" ~ connector ~ atomicTerm ~ rep1(atomicTerm) ~ ")" ^^ {
		case "(" ~ con ~ t ~ list ~ ")" => CompoundTerm(con, t :: list)
	}
	def infixCompTerm: Parser[CompoundTerm] = "(" ~ atomicTerm ~ rep1(connector ~ atomicTerm) ~ ")" ^^ {
		case "(" ~ t ~ list ~ ")" => CompoundTerm(list.head._1, t :: (for (x <- list) yield x._2))
	}
	def imgCompTerm1: Parser[CompoundTerm] = "(" ~ image ~ atomicTerm ~ "*" ~ atomicTerm ~ ")" ^^ {// ("*" ~ atomicTerm | atomicTerm ~ "*") ~ ")" ^^ {
		case "(" ~ img ~ rel ~ "*" ~ t ~ ")" => CompoundTerm(img, List(rel, Placeholder, t))
	}
	def imgCompTerm2: Parser[CompoundTerm] = "(" ~ image ~ atomicTerm ~ atomicTerm ~ "*" ~ ")" ^^ {// ("*" ~ atomicTerm | atomicTerm ~ "*") ~ ")" ^^ {
		case "(" ~ img ~ rel ~ t ~ "*" ~ ")" => CompoundTerm(img, List(rel, t, Placeholder))
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