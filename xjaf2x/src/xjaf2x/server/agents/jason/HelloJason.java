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

package xjaf2x.server.agents.jason;

import jason.asSyntax.Literal;
import java.util.List;
import java.util.Map;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.jboss.ejb3.annotation.Clustered;
import xjaf2x.server.agentmanager.Agent;
import xjaf2x.server.agentmanager.jason.JasonAgentI;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;

/**
 * Work in progress.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful(name = "xjaf2x_server_agents_jason_HelloJason")
@Remote(JasonAgentI.class)
@Clustered
public class HelloJason extends Agent implements JasonAgentI
{
	private static final long serialVersionUID = 1L;

	@Override
	public void init(Map<String, Object> args) throws Exception
	{
	}
	
	@Override
	public void onMessage(ACLMessage message)
	{
	}

	@Override
	public List<Literal> perceive()
	{
		System.out.println("perceive(): " + getAid().getRuntimeName() + " " + System.getProperty("jboss.node.name"));
		return null;
	}

	@Override
	public boolean act(String functor)
	{
		System.out.println("act(): " + getAid());
		return true;
	}
}
