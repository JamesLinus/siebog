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

package siebog.interaction.contractnet.example;

import java.util.Random;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import siebog.agents.Agent;
import siebog.interaction.contractnet.CallForProposal;
import siebog.interaction.contractnet.Participant;
import siebog.interaction.contractnet.Proposal;
import siebog.interaction.contractnet.Result;

/**
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic<a>
 */

@Stateful
@Remote(Agent.class)
public class ParticipantExample extends Participant {

	private static final long serialVersionUID = 1L;

	@Override
	public Proposal createProposal(CallForProposal cfp) {
		Proposal proposal = new Proposal();
		proposal.setInitiator(cfp.getInitiator());
		proposal.setParticipant(myAid);

		//calculate how long it will take
		Random rnd = new Random();
		int rndNum = rnd.nextInt(20 - 1 + 1) + 1;

		if (rndNum<10){
			proposal.setProposing(false);

			logger.info(myAid + ": I'm not bidding.");
		
			
		} else {
			proposal.setProposing(true);
			//Proposed time needed to finish the task
			proposal.setContent(Integer.toString(rndNum));
			proposal.setTimeEstimate(rndNum*1000l);
			logger.info(myAid + ": My bid is " + rndNum);
		}

		return proposal;
	}

	@Override
	public Result performTask(CallForProposal cfp) {
		String[] nums = cfp.getContent().split(",");
		int sum = 0;
		for (String num: nums){
			sum+=Integer.parseInt(num);
		}
		Result result  = new Result();

		//simulate failure in performing task

		Random rnd = new Random();
		int rndNum = rnd.nextInt(20 - 1 + 1) + 1;

		if (rndNum%4==0){
			result.setSuccesful(false);
			logger.info(myAid + ": Failure in preforming the task.");
		}else{
			result.setSuccesful(true);
			result.setContent(Integer.toString(sum));

			logger.info(myAid + ": Task succesfully preformed.");
		}
		return result;
	}

}
