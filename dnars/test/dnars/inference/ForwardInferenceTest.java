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

package dnars.inference;

import static dnars.TestUtils.TEST_KEYSPACE;
import static dnars.TestUtils.assertGraph;
import static dnars.TestUtils.create;
import static dnars.TestUtils.invert;
import org.junit.Test;
import dnars.base.Statement;
import dnars.base.StatementParser;
import dnars.base.Truth;
import dnars.graph.DNarsGraph;
import dnars.graph.DNarsGraphFactory;

public class ForwardInferenceTest
{
	@Test
	public void deduction_analogy()
	{
		// M -> P  
		//		S -> M	=> S -> P ded 
		//		S ~ M	=> S -> P ana
		DNarsGraph graph = DNarsGraphFactory.create(TEST_KEYSPACE, null);
		try
		{
			Statement[] kb = create(
				"cat -> animal (0.82, 0.91)",
				"water -> liquid (1.0, 0.9)",
				"tiger -> cat (0.5, 0.7)",
				"developer ~ job (1.0, 0.9)",
				"feline ~ cat (0.76, 0.83)"
			);
			for (Statement st: kb)
			{
				graph.statements().add(st);
				ForwardInference.deduction_analogy(graph, st);
			}
			Statement[] res = {
				invert(kb[3]),
				invert(kb[4]),
				StatementParser.apply("tiger -> animal " + kb[0].truth().deduction(kb[2].truth())),
				StatementParser.apply("feline -> animal " + kb[0].truth().analogy(kb[4].truth(), false))
			};
			assertGraph(graph, kb, res);
		} finally
		{
			graph.shutdown(true);
		}
	}
	
	@Test
	public void analogy_resemblance() 
	{
		// M ~ P ::
		//		S -> M	=> S -> P ana'
		//		S ~ M	=> S ~ P res
		DNarsGraph graph = DNarsGraphFactory.create(TEST_KEYSPACE, null);
		try
		{
			Statement[] kb = create(
				"developer -> job (0.6, 0.77)",
				"cat ~ feline (0.33, 0.51)",
				"tiger -> cat (0.95, 0.83)",
				"water -> liquid (0.63, 0.72)",
				"lion ~ cat (0.85, 0.48)"
			);
			for (Statement st: kb)
			{
				graph.statements().add(st);
				ForwardInference.analogy_resemblance(graph, st);
			}
			Statement st = StatementParser.apply("lion ~ feline " + kb[1].truth().resemblance(kb[4].truth()));
			Statement[] res = {
				invert(kb[1]),
				invert(kb[4]),
				StatementParser.apply("tiger -> feline " + kb[1].truth().analogy(kb[2].truth(), true)),
				st,
				invert(st)
			};
			assertGraph(graph, kb, res);
		} finally
		{
			graph.shutdown(true);
		}
	}
	
	@Test
	public void abduction_comparison_analogy()
	{
		// P -> M 
		//		S -> M	=> S -> P abd, S ~ P cmp
		//		S ~ M 	=> P -> S ana
		DNarsGraph graph = DNarsGraphFactory.create(TEST_KEYSPACE, null);
		try
		{
			Statement[] kb = create(
				"tiger -> cat (1.0, 0.9)",
				"water -> liquid (0.68, 0.39)",
				"developer -> job (0.93, 0.46)",
				"lion -> cat (0.43, 0.75)",
				"feline ~ cat (0.49, 0.52)"
			);
			for (Statement st: kb)
			{
				graph.statements().add(st);
				ForwardInference.abduction_comparison_analogy(graph, st);
			}
			Statement st = StatementParser.apply("lion ~ tiger " + kb[0].truth().comparison(kb[3].truth()));
			Statement[] res = {
				invert(kb[4]),
				StatementParser.apply("lion -> tiger " + kb[0].truth().abduction(kb[3].truth())),
				st,
				invert(st),
				StatementParser.apply("tiger -> feline " + kb[0].truth().analogy(kb[4].truth(), false)),
				StatementParser.apply("lion -> feline " + kb[3].truth().analogy(kb[4].truth(), false))
			};
			assertGraph(graph, kb, res);
		} finally
		{
			graph.shutdown(true);
		}
	}
	
	@Test
	public void compoundExtentional()
	{
		DNarsGraph graph = DNarsGraphFactory.create(TEST_KEYSPACE, null);
		try
		{
			Statement[] kb = create(
				"(cat x bird) -> eat (1.0, 0.9)",
				"tiger -> cat (1.0, 0.9)"
			);
			for (Statement st: kb)
			{
				graph.statements().add(st);
				ForwardInference.deduction_analogy(graph, st);
			}
	
			Truth ded = kb[0].truth().deduction(kb[1].truth());
			Statement[] res = { 
				StatementParser.apply("cat -> (/ eat * bird) (1.0, 0.9)"),
				StatementParser.apply("bird -> (/ eat cat *) (1.0, 0.9)"),
				StatementParser.apply("(tiger x bird) -> eat " + ded),
				StatementParser.apply("tiger -> (/ eat * bird) " + ded),
				StatementParser.apply("bird -> (/ eat tiger *) " + ded)
			};
			assertGraph(graph, kb, res);
		} finally
		{
			graph.shutdown(true);
		}
	}
}
