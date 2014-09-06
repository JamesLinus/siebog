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

import java.io.File
import java.io.PrintWriter

import com.hp.hpl.jena.rdf.model.RDFNode

import siebog.dnars.base.AtomicTerm
import siebog.dnars.base.CompoundTerm
import siebog.dnars.base.Connector.Product
import siebog.dnars.base.Copula.Inherit
import siebog.dnars.base.Statement
import siebog.dnars.base.StatementParser
import siebog.dnars.base.Truth

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
object DNarsConvert {

	def main(args: Array[String]): Unit = {
		if (args.length != 3) {
			println("I need three arguments: InputFile OutputFolder LinesPerFile")
			return
		}

		val input = new File(args(0))
		val outDir = new File(args(1))
		val linesPerFile = args(2).toInt

		val n = input.getName.lastIndexOf('.')
		val fileName = input.getName.substring(0, n)

		var fileIndex = 0
		var outStream = newPrintWriter(outDir, fileName, fileIndex)
		try {
			NTReader.read(input.getAbsolutePath(), (_, ntStat, counter) => {
				val statement = toDNarsStatement(ntStat)
				outStream.println(statement.toString)
				if (counter % linesPerFile == 0) {
					outStream.close
					fileIndex += 1
					outStream = newPrintWriter(outDir, fileName, fileIndex)
				}
				true
			})
		} finally {
			outStream.close
		}
	}

	private def newPrintWriter(dir: File, fileName: String, index: Int): PrintWriter = {
		val f = new File(dir, s"$fileName-$index.dnars")
		println(s"Writing to $f")
		new PrintWriter(f)
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