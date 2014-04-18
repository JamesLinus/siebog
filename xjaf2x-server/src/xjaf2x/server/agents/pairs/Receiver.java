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
import javax.ejb.Stateful;
import org.jboss.ejb3.annotation.Clustered;
import xjaf2x.server.agentmanager.agent.Agent;
import xjaf2x.server.agentmanager.agent.AgentI;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import xjaf2x.server.messagemanager.fipaacl.Performative;

/**
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful(name = "xjaf2x_server_agents_pairs_Receiver")
@Remote(AgentI.class)
@Clustered
public class Receiver extends Agent
{
	private static final long serialVersionUID = -677935957265970587L;
	private int primeLimit;

	@Override
	public void init(Serializable... args)
	{
		primeLimit = Integer.parseInt(args[0].toString());
	}

	@Override
	protected void onMessage(ACLMessage msg)
	{
		ACLMessage reply = msg.makeReply(Performative.INFORM);
		reply.setSender(myAid);
		reply.setContent(process());
		msm.post(reply);
	}
	
	private int process()
	{
		int primes = 0;
		for (int i = 1; i <= primeLimit; i++)
		{
			int j = 2;
			while (j <= i)
			{
				if (i % j == 0)
					break;
				++j;
			}
			if (j == i)
				++primes;
		}
		return primes;
	}
}
