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

import java.util.List;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.agents.Agent;
import siebog.interaction.contractnet.CallForProposal;
import siebog.interaction.contractnet.Initiator;
import siebog.interaction.contractnet.Proposal;
import siebog.interaction.contractnet.Result;

/**
 * @author <a href="jovanai.191@gmail.com">Jovana Ivkovic<a>
 */

@Stateful
@Remote(Agent.class)
public class InitiatorExample extends Initiator {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(InitiatorExample.class);
	private long needsToBeFinished;

	@Override
	public Proposal getOptimalProposal(List<Proposal> proposals) {
		// will look for the fastest
		int bestValue = 10000;
		Proposal bestProposal = null;
		for (Proposal p : proposals) {
			if (Integer.parseInt(p.getContent()) < bestValue) {
				bestProposal = p;
				bestValue = Integer.parseInt(p.getContent());
			}
		}
		LOG.info("Accepting proposal with value {}.", bestProposal.getContent());
		return bestProposal;
	}

	@Override
	public CallForProposal createCfp() {
		// he's asking for the sum of the numbers in the string
		CallForProposal cfp = new CallForProposal(myAid, "1,2,3,4,5,6,7,8,9");
		cfp.setReplyBy(System.currentTimeMillis() + 10 * 1000);
		needsToBeFinished = System.currentTimeMillis() + 30 * 1000;
		return cfp;
	}

	@Override
	public void failure() {
		LOG.info("No contractor was able to preform the action.");

	}

	@Override
	public void success(Result result) {
		LOG.info("The result of the task is {}.", result.getContent());

	}

}
