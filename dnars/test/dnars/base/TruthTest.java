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

package dnars.base;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TruthTest
{
	@Test
	public void testForward()
	{
		Truth t1 = new Truth(0.63, 0.84);
		Truth t2 = new Truth(0.79, 0.95);
		
		assertTruth(new Truth(0.5, 0.4),   t1.deduction(t2));
		assertTruth(new Truth(0.63, 0.39), t1.induction(t2));
		assertTruth(new Truth(0.79, 0.33), t1.abduction(t2));
		assertTruth(new Truth(0.54, 0.42), t1.comparison(t2));
		assertTruth(new Truth(0.50, 0.63), t1.analogy(t2, false));
		assertTruth(new Truth(0.50, 0.50), t1.analogy(t2, true));
		assertTruth(new Truth(0.50, 0.74), t1.resemblance(t2));
	}
	
	@Test
	public void testRevision()
	{
		Truth t1 = new Truth(0.63, 0.84);
		Truth t2 = new Truth(0.79, 0.95);
		Truth t3 = new Truth(0.53, 0.67);
		Truth t4 = new Truth(0.12, 0.31);
		
		assertTruth(new Truth(0.76, 0.96), t1.revision(t2));
		
		// test associativity
		Truth rev1 = t1.revision(t2).revision(t3).revision(t4);
		Truth rev2 = t1.revision(t2.revision(t3)).revision(t4);
		Truth rev3 = t1.revision(t2.revision(t3.revision(t4)));
		assertTruth(rev1, rev2);
		assertTruth(rev2, rev3);
	}
	
	private void assertTruth(Truth expected, Truth actual)
	{
		assertTrue(expected.closeTo(actual));
	}
}
