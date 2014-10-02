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

import java.util.List;
import java.util.logging.Logger;
import jason.asSemantics.ActionExec;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import siebog.jasonee.JasonEEAgArch;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class InitiatorAgArch extends JasonEEAgArch {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(InitiatorAgArch.class.getName());
	private int numPart;
	private long startTime;
	private int received;

	@Override
	public void init() throws Exception {
		numPart = Integer.parseInt(getOptions().get("numPart"));

		int primeLimit = Integer.parseInt(getOptions().get("primeLimit"));
		Literal lit = Literal.parseLiteral("primeLimit(" + primeLimit + ")");
		getTS().getAg().addBel(lit);
	}

	@Override
	public void act(ActionExec action, List<ActionExec> feedback) {
		Structure term = action.getActionTerm();
		boolean processed;
		switch (term.getFunctor()) {
		case "cfpStarted":
			startTime = System.currentTimeMillis();
			received = 0;
			processed = true;
			break;
		case "taskCompleted":
			++received;
			if (received == numPart)
				logger.info("Total time: " + (System.currentTimeMillis() - startTime) + " ms");
			processed = true;
			break;
		default:
			super.act(action, feedback);
			processed = false;
		}
		if (processed) {
			action.setResult(true);
			feedback.add(action);
		}
	}
}
