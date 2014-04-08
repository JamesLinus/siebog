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

package org.xjaf2x.server.config;

import java.io.Serializable;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.agentmanager.agent.jason.JasonAgentI;

/**
 * Description of a deployed agent.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public final class AgentDesc implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final String viewName = AgentI.class.getName();
	private static final String jasonViewName = JasonAgentI.class.getName();
	private final String family;
	private final boolean stateful;
	private final String jndiName;

	public AgentDesc(String family, boolean stateful, String appName, boolean isJason)
	{
		this.family = family;
		this.stateful = stateful;
		final String view = isJason ? jasonViewName : viewName;
		final String str = String.format("ejb:/%s//%s!%s", appName, family, view);
		if (stateful)
			jndiName = str + "?stateful";
		else
			jndiName = str;
	}

	public String getJndiName()
	{
		return jndiName;
	}

	public String getFamily()
	{
		return family;
	}

	public boolean isStateful()
	{
		return stateful;
	}
}
