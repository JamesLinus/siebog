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
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.jboss.ejb3.annotation.Clustered;
import xjaf2x.server.agentmanager.AID;
import xjaf2x.server.agentmanager.Agent;
import xjaf2x.server.agentmanager.AgentI;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import xjaf2x.server.messagemanager.fipaacl.Performative;

/**
 * ContractNet manager implementation.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful(name = "xjaf2x_server_agents_cnet_CNetManager")
@Remote(AgentI.class)
@Clustered
public class CNetManager extends Agent
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CNetManager.class.getName());
	private AID starter;
	private int numContr;
	private byte[] content;
	private long total;
	private int received;

	@Override
	public void onMessage(ACLMessage message)
	{
		switch (message.getPerformative())
		{
		case REQUEST:
			starter = message.getSender();
			
			String[] args = ((String) message.getContent()).split(" ");
			numContr = Integer.parseInt(args[0]);
			final int size = Integer.parseInt(args[1]);
			
			content = new byte[size];
			for (int i = 0; i < size; content[i++] = (byte) (Math.random() * 128))
				;

			total = 0;
			received = 0;
			sendCfps();
			break;
		case PROPOSE:
			ACLMessage accept = message.makeReply(Performative.ACCEPT_PROPOSAL);
			accept.setSender(getAid());
			accept.setContent(content);
			accept.setReplyWith(message.getInReplyTo());
			msm.post(accept);
			break;
		case INFORM:
			long time = System.nanoTime() - Long.parseLong(message.getInReplyTo());
			total += time / 1000000L;
			++received;
			if (received == numContr)
			{
				total = total / numContr;
				if (logger.isLoggable(Level.INFO))
					logger.info(String.format("Average time per message: [%d ms]", total));
				// send results to receiver
				ACLMessage reply = new ACLMessage(Performative.INFORM);
				reply.setSender(getAid());
				reply.addReceiver(starter);
				reply.setContent(total);
				msm.post(reply);
			}
			break;
		default:
			break;
		}
	}

	private void sendCfps()
	{
		if (logger.isLoggable(Level.INFO))
			logger.info("Sending CFPs to [" + numContr + "] contractors");
		new Thread() {
			@Override
			public void run()
			{
				for (int i = 0; i < numContr; i++)
				{
					ACLMessage cfp = new ACLMessage(Performative.CALL_FOR_PROPOSAL);
					cfp.setSender(getAid());
					cfp.addReceiver(new AID("org.xjaf2x.examples.cnet.CNetContractor", "C" + i));
					cfp.setContent(content);
					cfp.setProtocol("fipa-contract-net");
					cfp.setLanguage("fipa-sl");
					cfp.setReplyWith(System.nanoTime() + "");
					msm.post(cfp);
				}
			}
		}.start();
	}
}
