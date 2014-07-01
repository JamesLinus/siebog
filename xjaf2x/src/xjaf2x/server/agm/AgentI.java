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

package xjaf2x.server.agm;

import java.io.Serializable;
import xjaf2x.server.msm.fipa.acl.ACLMessage;

/**
 * Remote interface for all agents.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public interface AgentI extends Serializable
{
	AID getAid();

	/**
	 * Returns the name of the agent's host node.
	 * 
	 * @return
	 */
	String getNodeName();
	
	/**
	 * The remaining methods are for internal purposes only. You should never directly call or
	 * override any of them.
	 */
	
	void init(AID aid, Serializable... args) throws Exception;
	
	void handleMessage(ACLMessage msg);

	void processNextMessage();
	
	void terminate();
	
	void remove();
}
