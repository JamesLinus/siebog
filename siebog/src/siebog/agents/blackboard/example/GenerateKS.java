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

package siebog.agents.blackboard.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import siebog.agentmanager.Agent;
import siebog.agents.blackboard.Estimate;
import siebog.agents.blackboard.Event;
import siebog.agents.blackboard.KnowledgeSource;

/**
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic<a>
 */
@Stateful
@Remote(Agent.class)
public class GenerateKS extends KnowledgeSource {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(GenerateKS.class);

	@Override
	public void defineTriggers() {
		List<String> list = new ArrayList<String>();
		list.add("GENERATE");
		defineTriggers(list, "Numbers");
	}

	@Override
	public Estimate giveEstimate(Event e) {
		Estimate estimate = new Estimate();
		estimate.setAid(myAid);
		// generate a random estimate that expresses necessary time
		Random rnd = new Random();
		int num = rnd.nextInt(10 - 1 + 1) + 1;
		estimate.setContent(Integer.toString(num));
		estimate.setEvent(e);
		LOG.info("{}: my estimate is {}.", myAid, num);
		return estimate;
	}

	@Override
	public Event handleEvent(Event e) {
		// event is GENERATE
		// generating 10 random numbers
		LOG.info("{}: Generating numbers.", myAid);
		Random rnd = new Random();
		int num = rnd.nextInt(20 - 1 + 1) + 1;
		String numbers = Integer.toString(num);
		for (int i = 0; i < 9; i++) {
			num = rnd.nextInt(10 - 1 + 1) + 1;
			numbers += ";" + num;
		}
		Event newEvent = new Event();
		newEvent.setContent(numbers);
		newEvent.setName(e.getName());
		return newEvent;
	}

}
