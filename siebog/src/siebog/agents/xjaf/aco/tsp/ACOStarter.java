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

package siebog.agents.xjaf.aco.tsp;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import siebog.SiebogClient;
import siebog.agents.Agent;
import siebog.agents.AgentClass;
import siebog.agents.AgentInitArgs;
import siebog.agents.AgentManager;
import siebog.utils.ObjectFactory;

/**
 * Entry point for ACO example.
 * 
 * @author <a href="mailto:tntvteod@neobee.net">Teodor Najdan Trifunov</a>
 * @author <a href="mailto:milan.laketic@yahoo.com">Milan Laketic</a>
 */
public class ACOStarter {
	public static void main(String[] args) {
		int nAnts = 0;
		String path = "";
		if (args.length != 2) {
			System.out.println("I need 2 arguments: NumberOfAnts MapFile");
			nAnts = 5;
			path = "ulysses16.tsp";
		} else {
			nAnts = Integer.parseInt(args[0].toString());
			path = args[1];
		}

		SiebogClient.connect("localhost");

		final AgentManager agm = ObjectFactory.getAgentManager();
		AgentClass mapClass = new AgentClass(Agent.SIEBOG_MODULE, "Map");
		AgentInitArgs mapArgs = new AgentInitArgs("fileName=" + path);
		agm.startServerAgent(mapClass, "Map", mapArgs);

		for (int i = 1; i <= nAnts; ++i) {
			AgentClass agClass = new AgentClass(Agent.SIEBOG_MODULE, "Ant");
			agm.startServerAgent(agClass, "Ant" + i, new AgentInitArgs("host=localhost"));
		}
	}

	private static String getMapFilePath(String mapName) {
		URL url = ACOStarter.class.getResource("maps/" + mapName);
		try {
			return new File(url.toURI()).getAbsolutePath();
		} catch (URISyntaxException ex) {
			throw new IllegalArgumentException("Cannot load map " + mapName, ex);
		}
	}
}
