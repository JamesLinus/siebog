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

package siebog.interaction.blackboard.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.agents.Agent;
import siebog.interaction.blackboard.Estimate;
import siebog.interaction.blackboard.Event;
import siebog.interaction.blackboard.KnowledgeSource;

/**
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic<a>
 */
@Stateful
@Remote(Agent.class)
public class SquareKS extends KnowledgeSource {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(SquareKS.class);

	@Override
	public void defineTriggers() {
		List<String> list = new ArrayList<String>();
		list.add("SQUARE");
		defineTriggers(list, "Numbers");
	}

	@Override
	public Estimate giveEstimate(Event e) {
		Estimate estimate = new Estimate();
		estimate.setAid(myAid);
		// generate a random estimate that expresses necessary time
		Random rnd = new Random();
		int num = rnd.nextInt(20 - 1 + 1) + 1;
		estimate.setContent(Integer.toString(num));
		estimate.setEvent(e);
		LOG.info("{}: My estimate is {}.", myAid, num);
		return estimate;
	}

	@Override
	public Event handleEvent(Event e) {
		// event is SQUARE
		LOG.info("{}: Calculating squares.", myAid);
		String[] numbers = e.getContent().split(";");
		String squares = "";
		for (int i = 0; i < numbers.length; i++) {
			int num = Integer.parseInt(numbers[i]);
			squares += Integer.toString(num * num);
			if (i != numbers.length)
				squares += ";";
		}
		Event newEvent = new Event();
		newEvent.setContent(squares);
		newEvent.setName(e.getName());
		return newEvent;
	}

}
