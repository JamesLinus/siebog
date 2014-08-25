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

package siebog.agents.dnars;

import java.util.Arrays;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.server.xjaf.core.Agent;
import siebog.server.xjaf.dnarslayer.DNarsAgent;
import siebog.server.xjaf.dnarslayer.Event;
import siebog.server.xjaf.fipa.ACLMessage;

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateful
@Remote(Agent.class)
public class DNarsPing extends DNarsAgent {
	private static final long serialVersionUID = 1L;

	@Override
	public void onEvents(Event[] event) {
		System.out.println(Arrays.toString(event));
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		graph.addStatement("cat -> animal (1.0, 0.9)");
	}
}
