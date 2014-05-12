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

package xjaf2x.server.agents.ping;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.jboss.ejb3.annotation.Clustered;
import xjaf2x.server.agentmanager.AID;
import xjaf2x.server.agentmanager.Agent;
import xjaf2x.server.agentmanager.AgentI;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import xjaf2x.server.messagemanager.fipaacl.Performative;

/**
 * Example of a ping agent.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful(name = "xjaf2x_server_agents_ping_Ping")
@Remote(AgentI.class)
@Clustered
public class Ping extends Agent 
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void onMessage(ACLMessage msg)
	{
		if (msg.getPerformative() == Performative.REQUEST)
		{		
			logger.info("Ping @ [" + getNodeName() + "]");
			// send a request to the Pong agent		
			String pongName = msg.getContent().toString();
			AID pongAid = new AID("xjaf2x_server_agents_ping_Pong", pongName);
			ACLMessage pongMsg = new ACLMessage(Performative.REQUEST);
			pongMsg.setSender(myAid);
			pongMsg.addReceiver(pongAid);
			msm.post(pongMsg); // msm -> message manager
			// wait for the reply in a blocking fashion
			ACLMessage reply = receiveWait(0);
			logger.info("Pong says: " + reply.getContent());
		}
	}
}