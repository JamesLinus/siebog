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

import org.junit.Test;
import dnars.StatementParser;
import static dnars.TestUtils.*;
import dnars.base.Statement;
import dnars.base.Truth;
import dnars.gremlin.DNarsGraph;

public class ForwardInferenceTest
{
	@Test
	public void deduction_analogy()
	{
		// M -> P  
		//		S -> M	=> S -> P ded 
		//		S ~ M	=> S -> P ana
		DNarsGraph graph = createGraph();
		try
		{
			Statement[] kb = createAndAdd(graph,
				"cat -> animal (0.82, 0.91)",
				"water -> liquid (1.0, 0.9)",
				"tiger -> cat (0.5, 0.7)",
				"developer ~ job (1.0, 0.9)",
				"feline ~ cat (0.76, 0.83)"
			);
			for (Statement st: kb)
				ForwardInference.deduction_analogy(graph, st);
			Statement[] res = {
				StatementParser.apply("tiger -> animal " + kb[0].truth().deduction(kb[2].truth())),
				StatementParser.apply("feline -> animal " + kb[0].truth().analogy(kb[4].truth(), false))
			};
			assertGraph(graph, kb, res);
		} finally
		{
			graph.shutdown();
		}
	}
	
	@Test
	public void analogy_resemblance() 
	{
		// M ~ P ::
		//		S -> M	=> S -> P ana'
		//		S ~ M	=> S ~ P res
		DNarsGraph graph = createGraph();
		try
		{
			Statement[] kb = createAndAdd(graph,
				"developer -> job (1.0, 0.9)",
				"cat ~ feline (1.0, 0.9)",
				"tiger -> cat (1.0, 0.9)",
				"water -> liquid (1.0, 0.9)",
				"lion ~ cat (1.0, 0.9)"
			);
			for (Statement st: kb)
				ForwardInference.analogy_resemblance(graph, st);
			Statement[] res = {
				StatementParser.apply("tiger -> feline " + kb[1].truth().analogy(kb[2].truth(), true)),
				StatementParser.apply("lion ~ feline " + kb[1].truth().resemblance(kb[4].truth()))
			};
			assertGraph(graph, kb, res);
		} finally
		{
			graph.shutdown();
		}
	}
	
	@Test
	public void abduction_comparison_analogy()
	{
		// P -> M 
		//		S -> M	=> S -> P abd, S ~ P cmp
		//		S ~ M 	=> P -> S ana
		DNarsGraph graph = createGraph();
		try
		{
			Statement[] kb = createAndAdd(graph,
				"tiger -> cat (1.0, 0.9)",
				"water -> liquid (1.0, 0.9)",
				"developer -> job (1.0, 0.9)",
				"lion -> cat (1.0, 0.9)",
				"feline ~ cat (1.0, 0.9)"
			);
			for (Statement st: kb)
				ForwardInference.abduction_comparison_analogy(graph, st);
			Statement[] res = {
				StatementParser.apply("lion -> tiger " + kb[0].truth().abduction(kb[3].truth())),
				StatementParser.apply("lion ~ tiger " + kb[0].truth().comparison(kb[3].truth())),
				StatementParser.apply("lion -> feline " + kb[0].truth().analogy(kb[4].truth(), false)),
				StatementParser.apply("tiger -> feline " + kb[0].truth().analogy(kb[4].truth(), false)),
				StatementParser.apply("tiger -> lion " + kb[3].truth().abduction(kb[0].truth())),
				StatementParser.apply("tiger ~ lion " + kb[3].truth().comparison(kb[0].truth()))
			};
			assertGraph(graph, kb, res);
		} finally
		{
			graph.shutdown();
		}
	}
	
	@Test
	public void compoundExtentional()
	{
		DNarsGraph graph = createGraph();
		try
		{
			Statement[] kb = createAndAdd(graph,
				"(cat x bird) -> eat (1.0, 0.9)",
				"tiger -> cat (1.0, 0.9)"
			);
			for (Statement st: kb)
				ForwardInference.deduction_analogy(graph, st);
	
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
			graph.shutdown();
		}
	}
}
