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
import siebog.dnars.graph.DNarsGraphFactory
import siebog.dnars.inference.ForwardInference

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
object DBTest {

	def main(args: Array[String]): Unit = {
		val graph = DNarsGraphFactory.create("properties")
		try {
			val path = "/home/dejan/dev/siebog/dnars/src/siebog/dnars/inference/test/";
			val files = List("germany.nt", "mc2.nt", "physics.nt", "quantum_mechanics.nt", "theoretical_physics.nt")

			for (f <- files) {
				val in = new BufferedReader(new FileReader(path + f))
				var line = in.readLine()
				while (line != null) {
					val nt = NTReader.str2nt(line)
					val st = DNarsConvert.toDNarsStatement(nt)

					val conclusions = ForwardInference.conclusions(graph, Seq(st))
					for (c <- conclusions)
						println(c)

					line = in.readLine()
				}
				in.close()
			}
		} catch {
			case e: Exception => e.printStackTrace()
		} finally {
			graph.shutdown
			System.exit(0)
		}
	}

}