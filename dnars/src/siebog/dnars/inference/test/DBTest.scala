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

package siebog.dnars.inference.test

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.FileReader
import siebog.dnars.utils.importers.nt.NTReader
import siebog.dnars.utils.importers.nt.DNarsConvert
import siebog.dnars.inference.ForwardInference
import java.io.PrintWriter
import siebog.dnars.base.StatementParser
import siebog.dnars.base.Connector._
import siebog.dnars.base.Copula._
import siebog.dnars.base.CompoundTerm
import siebog.dnars.base.Statement
import siebog.dnars.inference.ResolutionEngine
import siebog.dnars.graph.DNarsGraphFactory
import scala.collection.mutable.ListBuffer
import siebog.dnars.graph.StructuralTransform

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
object DBTest {

	def main(args: Array[String]): Unit = {
		val properties = DNarsGraphFactory.create("properties")
		val conclusions = ListBuffer[Statement]()
		val newGraph = DNarsGraphFactory.create("new_graph")
		newGraph.eventManager.paused = true
		try {
			val path = "/home/dejan/dev/siebog/dnars/src/siebog/dnars/inference/test/";
			val files = List("germany.nt", "mc2.nt", "physics.nt", "quantum_mechanics.nt", "theoretical_physics.nt")

			for (f <- files) {

				val in = new BufferedReader(new FileReader(path + f))
				var line = in.readLine()
				while (line != null) {
					val nt = NTReader.str2nt(line)
					val st = DNarsConvert.toDNarsStatement(nt)

					val derived = ForwardInference.conclusions(properties, Seq(st))
					for (st <- derived) st match {
						case Statement(CompoundTerm(Product, Seq(a, b)), Similar, CompoundTerm(Product, Seq(c, d)), truth) =>
							if (a != c)
								conclusions += Statement(a, Similar, c, truth)
							if (b != d)
								conclusions += Statement(b, Similar, d, truth)
						case _ =>
							println(st)
					}

					line = in.readLine()
				}
				in.close()

				println("Done " + f)
			}

			val out = new PrintWriter("/home/dejan/tmp/final.nt")
			try {
				val derived =
					ForwardInference.conclusions(properties, conclusions)
						.filter(st => st.subj.toString.contains("Albert_Einstein") || st.pred.toString.contains("Albert_Einstein"))
						.sortWith((a, b) => a.truth.conf > b.truth.conf)
				val size = derived.size
				var i = 0
				for (st <- derived) {
					StructuralTransform.pack(st) match {
						case List(packed, _) =>
							if (!ResolutionEngine.hasAnswer(properties, packed))
								out.println(packed)
							newGraph.statements.add(packed)
						case _ =>
							if (!ResolutionEngine.hasAnswer(properties, st))
								out.println(st)
							newGraph.statements.add(st)
					}
					i += 1
					if (i % 1000 == 0)
						println(s"Completed $i of $size")
				}

				newGraph.printEdges
			} finally {
				out.close
			}
		} catch {
			case e: Exception => e.printStackTrace()
		} finally {
			properties.shutdown
			newGraph.shutdown
			newGraph.clear
			System.exit(0)
		}
	}

}