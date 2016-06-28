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

package siebog.clientmanager;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import siebog.agentmanager.AID;
import siebog.agentmanager.AgentInitArgs;
import siebog.agentmanager.AgentManager;
import siebog.utils.ObjectFactory;

/**
 * Default implementation of the WebClient Manager.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateless
@LocalBean
@Path("/webclient")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WebClientManager {
	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void acceptRadigostAgent(@FormParam("url") String url, @FormParam("aid") AID aid,
			@FormParam("state") String state) {
		AgentInitArgs args = new AgentInitArgs();
		args.put("url", url);
		args.put("state", state);
		AgentManager agm = ObjectFactory.getAgentManager();
		agm.startServerAgent(aid, args);
	}
}
