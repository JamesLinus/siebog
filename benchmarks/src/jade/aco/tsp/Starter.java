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

package jade.aco.tsp;

import java.util.logging.Level;
import java.util.logging.Logger;
import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

/**
 * Starter agent, entry point.
 * 
 * @author <a href="mailto:tntvteod@neobee.net">Teodor Najdan Trifunov</a>
 */
public class Starter extends Agent
{
	private static final Logger logger = Logger.getLogger(Starter.class.getName());
	private static final long serialVersionUID = -483774782852850348L;

	@Override
	protected void setup()
	{
		logger.fine("Starter agent running.");

		AgentContainer ac = null;
		try
		{
			ac = getContainerController();
			ac.createNewAgent("Map", "Map", null).start();

			Thread.sleep(100);

			int nAnts = (Integer) getArguments()[0];
			for (int i = 1; i < nAnts; ++i)
				ac.createNewAgent("Ant" + i, "Ant", null).start();

			logger.fine("Starter done.");
		} catch (StaleProxyException | InterruptedException ex)
		{
			logger.log(Level.SEVERE, "", ex);
		}
	}
}