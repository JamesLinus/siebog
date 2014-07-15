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

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static dnars.TestUtils.*;
import dnars.StatementParser;
import dnars.base.Statement;
import dnars.base.Truth;

public class DNarsGraphTest
{
	@Test
	public void testAddition()
	{
		DNarsGraph graph = createGraph();
		try
		{		
			Statement[] st = createAndAdd(graph,
				"cat -> animal (0.8, 0.77)",
				"cat -> mammal (1.0, 0.9)",
				"tiger -> cat (1.0, 0.9)"
			);

			assertGraph(graph, st, new Statement[0]);
			
			// check if revision is correctly applied when the statement exists
			graph.statements().add(st[0]);
			try
			{
				graph.statements().check(st[0]); // will throw an exception
			} catch (IllegalArgumentException ex)
			{
				Truth revised = st[0].truth().revision(st[0].truth());
				assertEquals(revised.toString(), ex.getMessage());
			}
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
			Statement[] kb = createAndAdd(graph,
				"(cat x bird) -> eat (1.0, 0.9)",
				"developer -> job (1.0, 0.9)",
				"dissolve -> (x water salt) (1.0, 0.9)"
			);
			Statement[] res = {
				StatementParser.apply("cat -> (/ eat * bird) (1.0, 0.9)"),
				StatementParser.apply("bird -> (/ eat cat *) (1.0, 0.9)"),
				StatementParser.apply("(\\ dissolve * salt) -> water (1.0, 0.9)"),
				StatementParser.apply("(\\ dissolve water *) -> salt (1.0, 0.9)")
			};
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
			Statement[] kb = createAndAdd(graph,
				"cat -> (/ eat * bird) (1.0, 0.9)",
				"poo -> (/ eat dog *) (1.0, 0.9)",
				"(\\ disolve * salt) -> liquid (1.0, 0.9)",
				"(\\ hate * cat) -> dog (1.0, 0.9)"
			);
			Statement[] res = {
				StatementParser.apply("(x cat bird) -> eat (1.0, 0.9)"),
				StatementParser.apply("bird -> (/ eat cat *) (1.0, 0.9)"),
				
				StatementParser.apply("(x dog poo) -> eat (1.0, 0.9)"),
				StatementParser.apply("dog -> (/ eat * poo) (1.0, 0.9)"),
				
				StatementParser.apply("disolve -> (x liquid salt) (1.0, 0.9)"),
				StatementParser.apply("(\\ disolve liquid *) -> salt (1.0, 0.9)"),
				
				StatementParser.apply("hate -> (x dog cat) (1.0, 0.9)"),
				StatementParser.apply("(\\ hate dog *) -> cat (1.0, 0.9)")
			};
			assertGraph(graph, kb, res);
		} finally
		{
			graph.shutdown();
		}
	}
}
