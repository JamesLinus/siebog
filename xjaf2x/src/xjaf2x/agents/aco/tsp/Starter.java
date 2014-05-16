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

package xjaf2x.agents.aco.tsp;

import java.io.Serializable;
import java.util.logging.Logger;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import xjaf2x.Global;
import xjaf2x.server.agentmanager.AID;
import xjaf2x.server.agentmanager.Agent;
import xjaf2x.server.agentmanager.AgentI;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;

/**
 * Starter agent, entry point.
 * 
 * @author <a href="mailto:tntvteod@neobee.net">Teodor Najdan Trifunov</a>
 * @author <a href="mailto:milan.laketic@yahoo.com">Milan Laketic</a>
 */
@Stateless(name = "xjaf2x_agents_aco_tsp_Starter")
@Remote(AgentI.class)
public class Starter extends Agent
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Starter.class.getName());

	@Override
	protected void onInit(Serializable... args)
	{
		logger.fine("Starter agent running.");

		AID mapAid = new AID(Global.SERVER, Global.getEjbName(Map.class), "Map");
		agm.start(mapAid, args[1]);

		int nAnts = Integer.parseInt(args[0].toString());
		for (int i = 1; i <= nAnts; ++i)
		{
			AID aid = new AID(Global.SERVER, Global.getEjbName(Ant.class), "Ant" + i);
			agm.start(aid);
		}

		logger.fine("Starter done.");
	}

	@Override
	public void onMessage(ACLMessage message)
	{
	}
}
