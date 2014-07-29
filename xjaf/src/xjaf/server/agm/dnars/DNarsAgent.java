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

package xjaf.server.agm.dnars;

import java.io.Serializable;
import dnars.events.EventObserver;
import dnars.graph.DNarsGraph;
import dnars.graph.DNarsGraphFactory;
//import dnars.graph.DNarsGraph;
//import dnars.graph.DNarsGraphFactory;
import xjaf.server.agm.Agent;

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class DNarsAgent extends Agent implements DNarsAgentI, EventObserver
{
	private static final long serialVersionUID = 1L;
	protected DNarsGraph kbase;
	
	@Override
	protected void onInit(Serializable... args)
	{
		super.onInit(args);
		if (args.length == 0)
			throw new IllegalArgumentException("DNars agents need to be initialized with the knowledgebase name.");
		kbase = DNarsGraphFactory.create(args[0].toString(), null);
		kbase.eventManager().addObserver(this);
	}
}
