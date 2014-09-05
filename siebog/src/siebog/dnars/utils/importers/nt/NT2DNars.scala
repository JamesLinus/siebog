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

package siebog.dnars.utils.importers.nt

import java.io.PrintWriter
import siebog.dnars.base.Statement
import siebog.dnars.base.CompoundTerm
import siebog.dnars.base.Truth
import siebog.dnars.base.StatementParser
import com.hp.hpl.jena.rdf.model.RDFNode
import siebog.dnars.base.AtomicTerm
import siebog.dnars.base.Copula._
import siebog.dnars.base.Connector._

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
object NT2DNars {
	def convert(ntInput: String): String = {
		val n = ntInput.lastIndexOf('.')
		val output = ntInput.substring(0, n) + ".dnars"
		val out = new PrintWriter(output)
		try {
			val total = NTReader.read(ntInput, (line, ntStat, counter) => {
				try {
					val statement = toDNarsStatement(ntStat)
					out.println(statement.toString)
					if (counter % 65536 == 0)
						println(s"Converted $counter statements...")
				} catch {
					case ex: Exception =>
						println(s"Error while converting statement $line")
						ex.printStackTrace
				}
				true
			})
			println(s"Done. Converted $total statements.")
			output
		} finally {
			out.close
		}
	}

	private def toDNarsStatement(ntStat: com.hp.hpl.jena.rdf.model.Statement): Statement = {
		// subject-predicate-object becomes
		// (x subject object) -> predicate
		val termSubj = getAtomicTerm(ntStat.getSubject)
		val termPred = getAtomicTerm(ntStat.getPredicate)
		val termObjt = getAtomicTerm(ntStat.getObject)

		val subj = CompoundTerm(Product, List(termSubj, termObjt))
		val statement = Statement(subj, Inherit, termPred, Truth(1.0, 0.9))

		// make sure everything's ok
		var str = statement.toString
		StatementParser(str)
	}

	private def getAtomicTerm(node: RDFNode): AtomicTerm = {
		val str = node.toString
			.trim
			.replaceAll("""\s""", "_")
			.replace("(", "{")
			.replace(")", "}")
		AtomicTerm(str)
	}
}