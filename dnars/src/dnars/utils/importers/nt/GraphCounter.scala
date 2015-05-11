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

package dnars.utils.importers.nt

import dnars.base.CompoundTerm
import dnars.base.Statement
import dnars.base.StatementParser
import scala.io.Source
import java.util.HashSet

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
object GraphCounter {
	def main(args: Array[String]): Unit = {
		if (args.length != 1) {
			println("I need one argument: InputFile")
			return
		}

		val vertices = new HashSet[String]()
		var edges = 0L
		var counter = 0
		Source
			.fromFile(args(0))
			.getLines()
			.foreach { line =>
				val statement = StatementParser(line)
				statement.allImages().foreach { st =>
					vertices.add(st.subj.id)
					vertices.add(st.pred.id)
					edges += 1
				}

				counter += 1
				if (counter % 100000 == 0)
					println(s"Processed $counter lines, ${vertices.size()} vertices, $edges edges...")
			}

		println("Done.")
		println(s"Total: ${vertices.size()} vertices, $edges edges.")
	}
}