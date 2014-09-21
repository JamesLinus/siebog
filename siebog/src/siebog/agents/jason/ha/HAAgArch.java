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

package siebog.agents.jason.ha;

import jason.asSemantics.ActionExec;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import siebog.jasonee.JasonEEAgArch;
import siebog.utils.ObjectFactory;

/**
 * Architecture of an agent which believes it should print out the host node's name at regular time intervals.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class HAAgArch extends JasonEEAgArch {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(HAAgArch.class.getName());
	private static final long INTERVAL = 2000;
	private static final Literal THE_BELIEF = Literal.parseLiteral("shouldPrintNodeName");
	private static final String DO_PRINT = "printNodeName";
	private static final String DO_REMINDME = "remindMeLater";
	private boolean includeBelief = true;

	@Override
	public List<Literal> perceive() {
		if (includeBelief) {
			includeBelief = false;
			return Collections.singletonList(THE_BELIEF);
		}
		return Collections.emptyList();
	}

	@Override
	public void act(ActionExec action, List<ActionExec> feedback) {
		final Structure term = action.getActionTerm();
		switch (term.getFunctor()) {
		case DO_PRINT:
			logger.info(getAgent().getAid() + "@" + System.getProperty("jboss.node.name"));
			action.setResult(true);
			break;
		case DO_REMINDME:
			ObjectFactory.getExecutorService().execute(new Runnable() {
				@Override
				public void run() {
					includeBelief = true;
					wake();
				}
			}, INTERVAL);
			break;
		default:
			action.setResult(false);
			action.setFailureReason(Literal.parseLiteral("unknownAction"), "Unknown action.");
		}
	}
}
