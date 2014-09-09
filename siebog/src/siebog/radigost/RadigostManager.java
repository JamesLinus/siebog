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

package siebog.radigost;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import siebog.radigost.entities.AgentState;
import siebog.radigost.websocket.bridges.BridgeException;
import siebog.utils.ObjectFactory;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Stateless
@LocalBean
@Path("/radigost")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RadigostManager {
	private static final Logger logger = Logger.getLogger(RadigostManager.class.getName());
	@PersistenceContext(name = "Radigost")
	private EntityManager em;

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void acceptRadigostAgent(@FormParam("url") String url, @FormParam("aid") String aid) {
		System.out.println(url + ":" + aid);
	}

	@PUT
	@Path("/bridge")
	public Response createBridge(@QueryParam("name") String name, @QueryParam("host") String host) {
		try {
			ObjectFactory.getBridgeManager().runBridge(name, host);
		} catch (BridgeException ex) {
			logger.log(Level.WARNING, "Error while creating a bridge.", ex);
			String msg = ex.getCause().getClass().getName() + ": " + ex.getCause().getMessage();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(msg).build();
		}
		return Response.status(Status.OK).build();
	}

	@GET
	@Path("/agentState/{aid}")
	public Response getAgentState(@PathParam("aid") String aid) {
		AgentState state = em.find(AgentState.class, aid);
		if (state != null)
			return Response.status(Status.OK).entity(state.getState()).build();
		return Response.status(Status.NOT_FOUND).build();
	}

	@POST
	@Path("/agentState/{aid}/{state}")
	public Response setAgentState(@PathParam("aid") String aid, @PathParam("state") String state) {
		AgentState obj = em.find(AgentState.class, aid);
		if (obj == null) {
			obj = new AgentState();
			obj.setAid(aid);
			obj.setState(state);
			em.persist(obj);
		} else {
			obj.setState(state);
			em.merge(obj);
		}
		return Response.status(Status.OK).build();
	}
}
