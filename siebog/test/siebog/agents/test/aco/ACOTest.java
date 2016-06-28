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

package siebog.agents.test.aco;

import siebog.agentmanager.Agent;
import siebog.agentmanager.AgentClass;
import siebog.agentmanager.AgentInitArgs;
import siebog.agents.test.TestClientBase;

/**
 * Entry point for ACO example.
 * 
 * @author <a href="mailto:tntvteod@neobee.net">Teodor Najdan Trifunov</a>
 * @author <a href="mailto:milan.laketic@yahoo.com">Milan Laketic</a>
 */
public class ACOTest extends TestClientBase {
	@Override
	public void test() {
		int nAnts = 0;
		String path = "";
		
		nAnts = 5;
		path = "ulysses16.tsp";

		AgentClass mapClass = new AgentClass(Agent.SIEBOG_MODULE, "ACOMap");
		AgentInitArgs mapArgs = new AgentInitArgs("fileName=" + path);
		agm.startServerAgent(mapClass, "Map", mapArgs);

		for (int i = 1; i <= nAnts; ++i) {
			AgentClass agClass = new AgentClass(Agent.SIEBOG_MODULE, "ACOAnt");
			agm.startServerAgent(agClass, "Ant" + i, new AgentInitArgs("host=localhost"));
		}
	}
	
	public static void main(String[] args) {

	}
}
