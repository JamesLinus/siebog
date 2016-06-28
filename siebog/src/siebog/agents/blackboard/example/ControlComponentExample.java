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

import java.util.List;

import siebog.agents.blackboard.ControlComponent;
import siebog.agents.blackboard.Estimate;
import siebog.agents.blackboard.Event;

/**
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic<a>
 */
public class ControlComponentExample extends ControlComponent {

	@Override
	public Estimate chooseBestProposal(List<Estimate> proposals) {
		//since KSs are estimating time, the control component is looking
		// for the minimum value
		int min = 1000;
		Estimate bestEstimate = null;
		for (Estimate e: proposals){
			int value = Integer.parseInt(e.getContent());
			if (value<min){
				min = value;
				bestEstimate = e;
			}
		}
		return bestEstimate;
	}

	@Override
	protected Event handleEvent(Event e) {
		Event event = new Event();
		event.setContent(e.getContent());
		event.setContentObj(e.getContentObj());
		switch(e.getName()){
		case "START":
			event.setName("GENERATE");;
			break;
		case "GENERATE":
			event.setName("SQUARE");
			break;
		case "SQUARE":
			event.setName("PRINT");
			break;
		case "PRINT":
			event.setName("DONE");
			break;
		default: 
			break;
		}
		return event;
	}

}
