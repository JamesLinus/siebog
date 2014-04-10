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

package org.xjaf2x.server.agents.protocols.cnet;

import org.xjaf2x.server.agentmanager.agent.Agent;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;

/**
 * Base class for <a
 * href="http://www.fipa.org/specs/fipa00029/SC00029H.pdf">FIPA Contract Net</a>
 * contractor/participant agents.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class CNetContractor extends Agent
{
	private static final long serialVersionUID = 1L;

	@Override
	public void onMessage(ACLMessage message)
	{
		switch (message.getPerformative())
		{
		case CALL_FOR_PROPOSAL:
			ACLMessage reply = getProposal(message);
			if (reply != null)
				msm.post(reply);
			break;
		case ACCEPT_PROPOSAL:
			ACLMessage result = onAcceptProposal(message);
			if (result != null)
				msm.post(result);
			break;
		case REJECT_PROPOSAL:
			onRejectProposal(message);
			break;
		default:
			break;
		}

	}

	protected abstract ACLMessage getProposal(ACLMessage cfp);

	protected abstract ACLMessage onAcceptProposal(ACLMessage proposal);

	protected abstract void onRejectProposal(ACLMessage proposal);
}
