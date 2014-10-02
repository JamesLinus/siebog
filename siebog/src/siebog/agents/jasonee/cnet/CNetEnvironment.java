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

package siebog.agents.jasonee.cnet;

import jason.NoValueException;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import siebog.jasonee.environment.UserEnvironment;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class CNetEnvironment extends UserEnvironment {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean executeAction(String agName, Structure act) {
		switch (act.getFunctor()) {
		case "analyzeCfp":
		case "processTask":
			try {
				final int limit = (int) ((NumberTerm) act.getTerm(0)).solve();
				countPrimes(limit);
				return true;
			} catch (NoValueException ex) {
				return false;
			}
		default:
			return false;
		}
	}

	private int countPrimes(int limit) {
		int primes = 0;
		for (int i = 1; i <= limit; i++) {
			int j = 2;
			while (j <= i) {
				if (i % j == 0)
					break;
				++j;
			}
			if (j == i)
				++primes;
		}
		return primes;
	}
}
