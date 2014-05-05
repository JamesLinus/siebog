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
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.jboss.ejb3.annotation.Clustered;
import xjaf2x.server.agentmanager.AID;
import xjaf2x.server.agentmanager.Agent;
import xjaf2x.server.agentmanager.AgentI;
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
	private AID receiver;
	private Serializable content;
	private String resultsServiceAddr;
	
	@Override
	protected void onInit(Serializable... args)
	{
		myIndex = Integer.parseInt(args[0].toString());
		numIterations = Integer.parseInt(args[1].toString());
		int primeLimit = Integer.parseInt(args[2].toString());
		// create message content
		int contentLength = Integer.parseInt(args[3].toString());
		content = makeContent(contentLength);
		// create receiver
		receiver = agm.start("xjaf2x_server_agents_pairs_Receiver", "R" + myIndex, primeLimit);
		resultsServiceAddr = args[4].toString();
	}

	@Override
	protected void onMessage(ACLMessage msg)
	{
		if (msg.getPerformative() == Performative.REQUEST)
		{
			iterationIndex = 0;
			String time = "" + System.currentTimeMillis();
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
				try
				{
					Registry reg = LocateRegistry.getRegistry(resultsServiceAddr);
					ResultsServiceI results = (ResultsServiceI) reg.lookup("ResultsService");
					results.add(avg);
					System.out.println("Pair " + myIndex + " done.");
				} catch (RemoteException | NotBoundException ex)
				{
					logger.log(Level.SEVERE, "Cannot connect to ResultsService.", ex);
				} finally
				{
					agm.stop(myAid);
					agm.stop(receiver);
				}
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
	
	private String makeContent(int length)
	{
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++)
			sb.append("A");
		return sb.toString();
	}
}
