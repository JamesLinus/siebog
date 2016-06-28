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

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.Form;

import siebog.agentmanager.AID;
import siebog.agentmanager.AgentClass;
import siebog.agentmanager.AgentInitArgs;
import siebog.agentmanager.AgentManager;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.MessageManager;
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
	private AgentManager agm = ObjectFactory.getAgentManager();
	private MessageManager msm = ObjectFactory.getMessageManager();
	
	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void acceptRadigostAgent(@FormParam("url") String url, @FormParam("aid") AID aid,
			@FormParam("state") String state) {
		AgentInitArgs args = new AgentInitArgs();
		args.put("url", url);
		args.put("state", state);
		agm.startServerAgent(aid, args, true);
	}
	
	@GET
	@Path("/running")
	public List<AID> getRunningAgents() {
		return agm.getRunningAgents();
	}
	
	@DELETE
	@Path("/running/{aid}")
	public void stopAgent(@PathParam("aid") AID aid) {
		agm.stopAgent(aid);
	}
	
	@PUT
	@Path("/running/{agClass}/{name}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public AID startAgent(@PathParam("agClass") AgentClass agClass,
			@PathParam("name") String name, @Form AgentInitArgs args,
			@QueryParam("replace") @DefaultValue("true") boolean replace) {
		return agm.startServerAgent(agClass, name, args);
	}
	
	@GET
	@Path("/classes")
	public List<AgentClass> getAvailableAgentClasses() {
		return agm.getAvailableAgentClasses();
	}
	
	@GET
	@Path("/messages")
	public List<String> getPerformatives() {
		return msm.getPerformatives();
	}

	@POST
	@Path("/messages")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void post(@FormParam("acl") ACLMessage msg) {
		msm.post(msg, 0);
	}
}
