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

package siebog.dnars.annotation;

import java.util.logging.Logger;
import siebog.dnars.base.Statement;
import siebog.dnars.base.Truth;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class DNarsAnnotationTest {
	private static final Logger logger = Logger.getLogger(DNarsAnnotationTest.class.getName());

	@Beliefs
	public String[] initBeliefs() {
		return new String[] {// @formatter:off
			"cat -> animal (1.0, 0.9)", 
			"tiger -> cat (1.0, 0.9)"
		}; // @formatter:on
	}

	@BeliefAdded(subj = "tiger", copula = "->", pred = "animal", truth = ".")
	public void tigerIsAnimal(Statement added) {
		logger.info("Tiger is an animal.");
	}

	@BeliefUpdated(pattern = ".")
	public void beliefUpdated(Statement st, Truth oldTruth) {
		logger.info("Belief updated: " + st + ", old truth-value: " + oldTruth);
	}

}
