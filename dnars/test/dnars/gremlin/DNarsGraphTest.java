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

package dnars.gremlin;

import static dnars.TestUtils.assertGraph;
import static dnars.TestUtils.createAndAdd;
import static dnars.TestUtils.createGraph;
import org.junit.Test;
import dnars.StatementParser;
import dnars.base.Statement;
import dnars.base.Truth;

public class DNarsGraphTest
{
	@Test
	public void testAtomicAddition()
	{
		DNarsGraph graph = createGraph();
		try
		{
			Statement[] st = createAndAdd(graph, // @formatter:off
				"cat -> animal (0.8, 0.77)",
				"cat ~ mammal (1.0, 0.9)",
				"tiger -> cat (1.0, 0.9)"
			); // @formatter:on

			// test if revision is correctly applied
			graph.statements().add(st[0]);
			Truth t = st[0].truth().revision(st[0].truth());
			st[0] = new Statement(st[0].subj(), st[0].copula(), st[0].pred(), t);

			assertGraph(graph, st, new Statement[0]);
		} finally
		{
			graph.shutdown();
		}
	}

	@Test
	public void testCompoundAddition()
	{
		DNarsGraph graph = createGraph();
		try
		{
			Statement[] kb = createAndAdd(graph, // @formatter:off
				"(cat x bird) -> eat (0.66, 0.93)",
				"dissolve -> (water x salt) (0.73, 0.52)",
				"poo -> (/ eat dog *) (0.13, 0.44)",
				"(\\ hate * cat) -> dog (0.85, 0.9)"
			); // @formatter:on

			// revision
			Truth[] t = new Truth[kb.length];
			for (int i = 0; i < kb.length; i++)
			{
				graph.statements().add(kb[i]);
				t[i] = kb[i].truth().revision(kb[i].truth());
				kb[i] = new Statement(kb[i].subj(), kb[i].copula(), kb[i].pred(), t[i]);
			}

			Statement[] res = {// @formatter:off
				StatementParser.apply("cat -> (/ eat * bird) " + t[0]),
				StatementParser.apply("bird -> (/ eat cat *) " + t[0]),
				StatementParser.apply("(\\ dissolve * salt) -> water " + t[1]),
				StatementParser.apply("(\\ dissolve water *) -> salt " + t[1]),
				StatementParser.apply("(dog x poo) -> eat " + t[2]),
				StatementParser.apply("dog -> (/ eat * poo) " + t[2]),
				StatementParser.apply("hate -> (dog x cat) " + t[3]),
				StatementParser.apply("(\\ hate dog *) -> cat " + t[3])
			}; // @formatter:on
			assertGraph(graph, kb, res);
		} finally
		{
			graph.shutdown();
		}
	}

	@Test
	public void testUnpack()
	{
		DNarsGraph graph = createGraph();
		try
		{
			Statement[] kb = createAndAdd(graph, // @formatter:off
				"(cat x bird) -> eat (1.0, 0.9)",
				"developer -> job (1.0, 0.9)",
				"dissolve -> (x water salt) (1.0, 0.9)"
			); 
			Statement[] res = {
				StatementParser.apply("cat -> (/ eat * bird) (1.0, 0.9)"),
				StatementParser.apply("bird -> (/ eat cat *) (1.0, 0.9)"),
				StatementParser.apply("(\\ dissolve * salt) -> water (1.0, 0.9)"),
				StatementParser.apply("(\\ dissolve water *) -> salt (1.0, 0.9)")
			}; // @formatter:on
			assertGraph(graph, kb, res);
		} finally
		{
			graph.shutdown();
		}
	}

	@Test
	public void testPack()
	{
		DNarsGraph graph = createGraph();
		try
		{
			Statement[] kb = createAndAdd(graph, // @formatter:off
				"cat -> (/ eat * bird) (1.0, 0.9)",
				"poo -> (/ eat dog *) (1.0, 0.9)",
				"(\\ dissolve * salt) -> liquid (1.0, 0.9)",
				"(\\ hate * cat) -> dog (1.0, 0.9)"
			);
			Statement[] res = {
				StatementParser.apply("(x cat bird) -> eat (1.0, 0.9)"),
				StatementParser.apply("bird -> (/ eat cat *) (1.0, 0.9)"),
				
				StatementParser.apply("(x dog poo) -> eat (1.0, 0.9)"),
				StatementParser.apply("dog -> (/ eat * poo) (1.0, 0.9)"),
				
				StatementParser.apply("dissolve -> (x liquid salt) (1.0, 0.9)"),
				StatementParser.apply("(\\ dissolve liquid *) -> salt (1.0, 0.9)"),
				
				StatementParser.apply("hate -> (x dog cat) (1.0, 0.9)"),
				StatementParser.apply("(\\ hate dog *) -> cat (1.0, 0.9)")
			}; // @formatter:on
			assertGraph(graph, kb, res);
		} finally
		{
			graph.shutdown();
		}
	}
}
