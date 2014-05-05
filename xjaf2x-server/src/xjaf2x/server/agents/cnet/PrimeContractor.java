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

package xjaf2x.server.agents.cnet;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.jboss.ejb3.annotation.Clustered;
import xjaf2x.server.agentmanager.AgentI;
import xjaf2x.server.agents.protocols.cnet.CNetContractor;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import xjaf2x.server.messagemanager.fipaacl.Performative;

/**
 * ContractNet contractor for calculating prime numbers.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful(name = "xjaf2x_server_agents_cnet_PrimeContractor")
@Remote(AgentI.class)
@Clustered
public class PrimeContractor extends CNetContractor
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(PrimeContractor.class.getName());

	@PostConstruct
	public void postConstruct()
	{
		if (logger.isLoggable(Level.INFO))
			logger.info("CNetContractor started @" + System.getProperty("jboss.node.name"));
	}
	
	private String process(String content)
	{
		long sum = 0;
		for (int i = 0; i < content.length(); i++)
			sum += content.codePointAt(i);
		return "" + sum;
	}
	
	@Override
	protected ACLMessage getProposal(ACLMessage cfp)
	{
		ACLMessage proposal = cfp.makeReply(Performative.PROPOSE);
		proposal.setContent(process((String) cfp.getContent()));
		return proposal;
	}

	@Override
	protected ACLMessage onAcceptProposal(ACLMessage proposal)
	{
		ACLMessage result = proposal.makeReply(Performative.INFORM);
		result.setContent(process((String) proposal.getContent()));
		return result;
	}

	@Override
	protected void onRejectProposal(ACLMessage proposal)
	{
	}
}
