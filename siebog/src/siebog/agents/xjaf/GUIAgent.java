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

package siebog.agents.xjaf;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import siebog.agents.Agent;
import siebog.agents.XjafAgent;
import siebog.interaction.ACLMessage;

/**
 * TODO add a description
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateless
@Remote(Agent.class)
@Path("/guiagent")
@Produces(MediaType.APPLICATION_JSON)
@LocalBean
public class GUIAgent extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private static final Queue<ACLMessage> received = new LinkedBlockingQueue<>();

	@GET
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public ACLMessage getMessage() {
		return received.poll();
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		received.add(msg);
	}
}
