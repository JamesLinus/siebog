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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
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
 * Main (entry) agent.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful(name = "xjaf2x_server_agents_cnet_CNetStarter")
@Remote(AgentI.class)
@Clustered
public class CNetStarter extends Agent
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CNetStarter.class.getName());
	
	private static final int[] NUM_CONTRACTORS = {
		//100, 200, 400, 800, 1600, 3200, 6400
		10
	};
	private static final int NUM_ITERATIONS = 1;
	private static final int CONTENT_SIZE = 65536;
	
	private int contrIndex;
	private int iteration;
	private List<AID> contractors = new ArrayList<>();
	private AID manager;

	@Override
	public void onMessage(ACLMessage message)
	{
		switch (message.getPerformative())
		{
		case REQUEST:
			iteration = 1;
			contrIndex = 0;
			start();
			break;
		case INFORM:
			long time = (Long) message.getContent();
			stop(time);
			++iteration;
			if (iteration > NUM_ITERATIONS)
			{
				++contrIndex;
				if (contrIndex >= NUM_CONTRACTORS.length)
				{
					if (logger.isLoggable(Level.INFO))
						logger.info("Done!");
					// stop agents
					for (AID aid : contractors)
						agm.stop(aid);
					contractors.clear();
					agm.stop(manager);
					return;
				}
				iteration = 1;
			}
			start();
			break;
		default:
			break;
		}
	}
	
	private void start()
	{
		final int numContr = NUM_CONTRACTORS[contrIndex];
		
		if (logger.isLoggable(Level.INFO))
			logger.info(String.format("Iteration %d of %d, %d contractors", iteration, NUM_ITERATIONS, numContr));
		
		// create new contractors
		for (int i = contractors.size(); i < numContr; i++)
		{
			AID aid = agm.start("org.xjaf2x.examples.cnet.CNetContractor", "C" + i);
			contractors.add(aid);
		}
		
		if (manager == null)
		{
			if (logger.isLoggable(Level.INFO))
				logger.info("Starting manager");
			manager = agm.start("org.xjaf2x.examples.cnet.CNetManager", "Manager");
		}
		
		// go!
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.setSender(getAid());
		msg.addReceiver(manager);
		msg.setContent(numContr + " " + CONTENT_SIZE);
		msm.post(msg);
	}
	
	private void stop(long time)
	{
		final int numContr = NUM_CONTRACTORS[contrIndex];
		
		String fileName = System.getProperty("user.home").replace('\\', '/');
		if (!fileName.endsWith("/"))
			fileName += "/";
		fileName += "cnet.txt";
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true))))
		{
			out.printf("%d\t%d\t%d\n", iteration, numContr, time);
		} catch (Exception ex)
		{
		}
	}
}
