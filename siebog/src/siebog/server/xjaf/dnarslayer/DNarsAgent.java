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

package siebog.server.xjaf.dnarslayer;

import java.util.Map;
import siebog.server.xjaf.agents.base.Agent;
import siebog.server.xjaf.agents.fipa.acl.ACLMessage;
import siebog.server.xjaf.agents.fipa.acl.Performative;

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class DNarsAgent extends Agent 
{
	private static final long serialVersionUID = 1L;
	protected DNarsGraphI graph;
	
	@Override
	protected void onInit(Map<String, String> args)
	{
		super.onInit(args);
		String domain = (String) args.get("domain");
		if (domain == null)
			domain = myAid.toString();
		try
		{
			graph = DNarsGraphFactory.create(domain);
			graph.addObserver(myAid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	@Override
	protected boolean filter(ACLMessage msg)
	{
		if (msg.getPerformative() == Performative.INFORM)
		{
			Event[] events = (Event[]) msg.getContent();
			onEvents(events);
			return false;
		}
		return true;
	}
	
	protected abstract void onEvents(Event[] events);
}
