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
import xjaf2x.server.agentmanager.agent.AID;
import xjaf2x.server.agentmanager.agent.Agent;
import xjaf2x.server.agentmanager.agent.AgentI;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import xjaf2x.server.messagemanager.fipaacl.Performative;

/**
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful(name = "xjaf2x_server_agents_pairs_Sender")
@Remote(AgentI.class)
@Clustered
public class Sender extends Agent
{
	private static final long serialVersionUID = -5648061637952026195L;
	private int myIndex;
	private int numIterations;
	private int iterationIndex;
	private Serializable content;
	private AID receiver;
	
	@Override
	protected void onInit(Serializable... args)
	{
		myIndex = Integer.parseInt(args[0].toString());
		numIterations = Integer.parseInt(args[1].toString());
		receiver = new AID("xjaf2x_server_agents_pairs_Receiver", "R" + myIndex);
	}

	@Override
	protected void onMessage(ACLMessage msg)
	{
		if (msg.getPerformative() == Performative.REQUEST)
		{
			iterationIndex = 0;
			String time = "" + System.currentTimeMillis();
			content = msg.getContent();
			postMsg(time);
		}
		else
		{
			if (++iterationIndex < numIterations)
				postMsg(msg.getInReplyTo());
			else
			{
				long avg = System.currentTimeMillis() - Long.parseLong(msg.getInReplyTo());
				avg /= numIterations;
				logger.warning("S" + myIndex + ": " + avg);
				agm.stop(myAid);
				agm.stop(receiver);
			}
		}
	}
	
	private void postMsg(String time)
	{
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.setSender(myAid);
		msg.addReceiver(receiver);
		msg.setContent(content);
		msg.setReplyWith(time);
		msm.post(msg);		
	}
}
