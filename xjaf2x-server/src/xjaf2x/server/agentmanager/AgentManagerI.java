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

package xjaf2x.server.agentmanager;

import java.io.Serializable;
import java.util.List;
import xjaf2x.server.agentmanager.agent.AID;
import xjaf2x.server.agentmanager.agent.jason.JasonAgentI;

/**
 * Remote interface of the agent manager.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public interface AgentManagerI extends Serializable
{
	/**
	 * Runs a new instance of an agent.
	 * 
	 * @param family Agent family name.
	 * @param runtimeName Runtime name of this agent.
	 * @param args Optional initialization arguments to pass to the agent.
	 * @return AID instance on success, null otherwise.
	 */
	AID startAgent(String family, String runtimeName, Serializable... args);

	JasonAgentI startJasonAgent(String family, String runtimeName, Serializable[] args);

	/**
	 * Terminates an active agent.
	 * 
	 * @param aid AID object.
	 */
	void stopAgent(AID aid);

	/**
	 * Returns the list of running agents.
	 * 
	 * @return List of agent AIDs.
	 */
	List<AID> getRunning();

	/**
	 * Returns the list of running agents whose AIDs match the provided pattern.
	 * 
	 * @param pattern
	 * @return
	 */
	List<AID> getRunning(AID pattern);

	/**
	 * Returns the list of deployed agent families.
	 * 
	 * @return List of available agent family names.
	 */
	List<String> getFamilies();
}
