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

package siebog.agentmanager;

import java.io.Serializable;
import java.util.List;

import siebog.utils.ObjectField;

/**
 * Remote interface of the agent manager.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public interface AgentManager extends Serializable {
	/**
	 * Equivalent to startServerAgent(aid, args, true)
	 */
	void startServerAgent(AID aid, AgentInitArgs args, boolean replace);

	AID startServerAgent(AgentClass agClass, String runtimeName, AgentInitArgs args);

	AID startClientAgent(AgentClass agClass, String runtimeName, AgentInitArgs args);

	void stopAgent(AID aid);

	List<AID> getRunningAgents();

	AID getAIDByRuntimeName(String runtimeName);

	List<AgentClass> getAvailableAgentClasses();

	void pingAgent(AID aid);

	void reconstructAgent(List<ObjectField> agent);

	void move(AID aid, String host);
	
	void clone(AID aid, String host);
}
