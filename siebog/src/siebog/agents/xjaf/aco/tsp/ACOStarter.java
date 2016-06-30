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

import org.slf4j.Logger;

import siebog.SiebogClient;
import siebog.agents.Agent;
import siebog.agents.AgentClass;
import siebog.agents.AgentInitArgs;
import siebog.agents.AgentManager;
import siebog.utils.LoggerUtil;
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
		int nAntsPerMap = 0;
		int nodesPerMap = 0;
		int nMaps = 1;
		String path = "";
				
		if (args.length != 2) {
			System.out.println("I need 2 arguments: NumberOfAnts MapFile");
			nAnts = 10;
			nAntsPerMap = 16;
			nMaps = 4;
			nodesPerMap = 4;
			path = "ulysses16.tsp";
		} else {
			nAnts = Integer.parseInt(args[0].toString());
			
			path = args[1].split(";")[0];
			nMaps = Integer.parseInt(args[1].split(";")[1]);
			nodesPerMap = Integer.parseInt(args[1].split(";")[2]);
			nAntsPerMap = Integer.parseInt(args[1].split(";")[3]);
			nAnts = nMaps * nAntsPerMap;
		}

		SiebogClient.connect("192.168.0.12");
		
		final AgentManager agm = ObjectFactory.getAgentManager();

		
		for(int i = 1; i <= nMaps; i++){
			AgentClass mapClass = new AgentClass(Agent.SIEBOG_MODULE, "Map");
			
			AgentInitArgs mapArgs = new AgentInitArgs("fileName=" + path + "&nMaps:" + nMaps + "&nodesPerMap:" + nodesPerMap + "&nodeStartIndex:" + (((i-1)*nodesPerMap)+1));
			
			System.out.println(mapArgs.get("fileName", null));
			agm.startServerAgent(mapClass, "Map" + i, mapArgs);
			
		}
		int mapIndex = 0;
		for (int i = 1; i <= nAntsPerMap*nMaps; ++i) {
			if((i-1)%nAntsPerMap == 0){
				mapIndex++;
			}
			AgentClass agClass = new AgentClass(Agent.SIEBOG_MODULE, "Ant");
			agm.startServerAgent(agClass, "Ant" + i, new AgentInitArgs("host=localhost", "map=Map" + mapIndex));
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
