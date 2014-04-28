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

package xjaf2x.server.agents.pairs;

import java.io.Serializable;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import org.jboss.ejb3.annotation.Clustered;
import xjaf2x.server.agentmanager.agent.AID;
import xjaf2x.server.agentmanager.agent.Agent;
import xjaf2x.server.agentmanager.agent.AgentI;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import xjaf2x.server.messagemanager.fipaacl.Performative;

/**
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateless(name = "xjaf2x_server_agents_pairs_Starter")
@Remote(AgentI.class)
@Clustered
public class Starter extends Agent
{
	private static final long serialVersionUID = -4972585393971070318L;
	
	@Override
	protected void onInit(Serializable... args)
	{
		int numPairs = Integer.parseInt(args[0].toString());
		int numIterations = Integer.parseInt(args[1].toString());
		int primeLimit = Integer.parseInt(args[2].toString());
		int contentLength = Integer.parseInt(args[3].toString());
		
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.setContent(makeContent(contentLength));
		
		for (int i = 0; i < numPairs; i++)
		{
			agm.start("xjaf2x_server_agents_pairs_Receiver", "R" + i, primeLimit);
			AID aid = agm.start("xjaf2x_server_agents_pairs_Sender", "S" + i, i, numIterations);
			msg.addReceiver(aid);
		}
		
		msm.post(msg);
		agm.stop(myAid);
	}

	@Override
	protected void onMessage(ACLMessage msg)
	{
	}
	
	private String makeContent(int length)
	{
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++)
			sb.append("A");
		return sb.toString();
	}

}
