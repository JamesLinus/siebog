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

import static dnars.TestUtils.createAndAdd;
import static dnars.TestUtils.createGraph;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import scala.Option;
import dnars.base.AtomicTerm;
import dnars.base.Statement;
import dnars.base.StatementParser;
import dnars.base.Term;
import dnars.graph.DNarsGraph;

public class QuestionsTest
{
	@Test
	public void testAnswer()
	{
		DNarsGraph graph = createGraph();
		try
		{
			createAndAdd(graph, // @formatter:off 
				"cat -> animal (1.0, 0.9)",
				"developer ~ job (1.0, 0.9)"
			); // @formatter:on
			
			assertAnswer(graph, "? -> cat", null);
			assertAnswer(graph, "? -> animal", new AtomicTerm("cat"));
			assertAnswer(graph, "cat -> ?", new AtomicTerm("animal"));
			assertAnswer(graph, "animal -> ?", null);
			assertAnswer(graph, "water -> ?", null);
			assertAnswer(graph, "developer ~ ?", new AtomicTerm("job"));
			assertAnswer(graph, "? ~ developer", new AtomicTerm("job"));
		} finally
		{
			graph.shutdown();
		}
	}
	
	@Test
	public void testTraversal()
	{
		DNarsGraph graph = createGraph();
		try
		{
			createAndAdd(graph, // @formatter:off
				"cat -> animal (1.0, 0.9)",
				"mammal ~ animal (1.0, 0.9)",
				"animal -> being (1.0, 0.9)"
			); // @formatter:on
			
			assertPath(graph, "cat -> being", true);
			assertPath(graph, "animal ~ mammal", true);
			assertPath(graph, "cat -> mammal", true);
			assertPath(graph, "cat ~ mammal", false);
			assertPath(graph, "cat ~ being", false);
		} finally
		{
			graph.shutdown();
		}
	}
	
	private void assertPath(DNarsGraph graph, String statement, boolean exists)
	{
		Statement st = StatementParser.apply(statement + " (1.0, 0.9)");
		assertEquals(exists, Questions.exists(graph, st));
	}
	
	private void assertAnswer(DNarsGraph graph, String question, Term answer)
	{
		Option<Term> a = Questions.answer(graph, StatementParser.apply(question));
		if (a == Option.apply((Term) null))
			assertEquals(answer, null);
		else
			assertEquals(answer, a.get());
	}
}
