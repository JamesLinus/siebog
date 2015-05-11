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

package dnars.base

import org.junit.Test

import dnars.DNarsTestUtils.assertSeq
import dnars.DNarsTestUtils.create

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
class StatementTest {
	@Test
	def testImages(): Unit = {
		val st1 = "(cat x bird) -> eat (1.0, 0.9)"
		assertSeq(
			StatementParser(st1).allImages(),
			create(st1,
				"cat -> (/ eat * bird) (1.0, 0.9)",
				"bird -> (/ eat cat *) (1.0, 0.9)"))

		val st2 = "dissolve -> (x water salt) (1.0, 0.9)"
		assertSeq(
			StatementParser(st2).allImages(),
			create(st2,
				"(\\ dissolve * salt) -> water (1.0, 0.9)",
				"(\\ dissolve water *) -> salt (1.0, 0.9)"))

		val st3 = "cat -> animal (1.0, 0.9)"
		assertSeq(
			StatementParser(st3).allImages(),
			create(st3))
	}
}