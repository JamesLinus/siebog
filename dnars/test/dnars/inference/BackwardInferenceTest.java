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
import static org.junit.Assert.*;
import static dnars.TestUtils.*;
import dnars.StatementParser;
import dnars.base.Statement;
import dnars.gremlin.DNarsGraph;

/**
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class BackwardInferenceTest
{
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
			
			assertPath(graph, "cat -> being");
			//assertPath(graph, "animal ~ mammal");
		} finally
		{
			graph.shutdown();
		}
	}
	
	private void assertPath(DNarsGraph graph, String statement)
	{
		Statement st = StatementParser.apply(statement + " (1.0, 0.9)");
		assertTrue(BackwardInference.findPath(graph, st));
	}
}
