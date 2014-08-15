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

package siebog.server.xjaf.managers;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import siebog.server.xjaf.agents.base.AID;
import siebog.server.xjaf.agents.base.AgentClass;

/**
 * Remote interface of the agent manager.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public interface AgentManagerI extends Serializable
{

	/**
	 * Starts a new agent.
	 * 
	 * @param agClass AgentClass object.
	 * @param name Runtime name.
	 * @param args Optional initialization arguments to be passed to the agent.
	 * @return AID of the new agent.
	 * @throws IllegalArgumentException if the agent could not be started.
	 */
	AID start(AgentClass agClass, String name, Map<String, String> args);

	/**
	 * Terminates an active agent.
	 * 
	 * @param aid AID object.
	 */
	void stop(AID aid);

	/**
	 * Returns the list of running agents.
	 * 
	 * @return List of agent AIDs.
	 */
	List<AID> getRunning();
	
	AID getAIDByName(String name);

	/**
	 * Returns the list of deployed agents.
	 * 
	 * @return List of AIDs, with runtime names set to null.
	 */
	List<AgentClass> getDeployed();
}
