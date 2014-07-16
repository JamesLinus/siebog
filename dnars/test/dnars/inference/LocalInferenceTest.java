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
import dnars.StatementParser;
import dnars.base.AtomicTerm;
import dnars.base.Term;
import dnars.gremlin.DNarsGraph;

public class LocalInferenceTest
{
	@Test
	public void test1()
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
	
	private void assertAnswer(DNarsGraph graph, String question, Term answer)
	{
		Option<Term> a = LocalInference.answer(graph, StatementParser.apply(question));
		if (a == Option.apply((Term) null))
			assertEquals(answer, null);
		else
			assertEquals(answer, a.get());
	}
}
