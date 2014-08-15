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

package agents.ping;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.server.xjaf.agents.base.AID;
import siebog.server.xjaf.agents.base.Agent;
import siebog.server.xjaf.agents.base.AgentI;
import siebog.server.xjaf.agents.fipa.acl.ACLMessage;
import siebog.server.xjaf.agents.fipa.acl.Performative;

/**
 * Example of a ping agent.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(AgentI.class)
public class Ping extends Agent 
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void onMessage(ACLMessage msg)
	{
		if (msg.getPerformative() == Performative.REQUEST)
		{		
			logger.info(myAid.toString());
			// send a request to the Pong agent		
			final String content = msg.getContent().toString();
			int n = content.indexOf('@');
			String name = content.substring(0, n);
			String hap = content.substring(n + 1);
			AID pongAid = new AID(name, hap);
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