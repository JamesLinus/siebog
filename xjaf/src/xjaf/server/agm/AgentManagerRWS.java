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

package xjaf.server.agm;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import xjaf.server.Global;

/**
 * RESTful interface for the agent manager.
 * 
 * @author <a href="rade.milovanovic@hotmail.com">Rade Milovanovic</a>
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Path("/agm")
public class AgentManagerRWS
{
	private static final Logger logger = Logger.getLogger(AgentManagerRWS.class.getName());

	@GET
	@Produces("application/json")
	@Path("/getdeployed")
	@SuppressWarnings("unchecked")
	public String getDeployed()
	{
		JSONObject obj = new JSONObject();
		JSONArray list = new JSONArray();
		try
		{
			List<AID> deployed = Global.getAgentManager().getDeployed();
			for (AID aid : deployed)
				list.add(aid.toString());
		} catch (Exception e)
		{
			logger.log(Level.WARNING, "Error while loading deployed agents.", e);
		}
		obj.put("families", list);
		return obj.toJSONString();
	}

	@GET
	@Produces("application/json")
	@Path("/getrunning")
	@SuppressWarnings("unchecked")
	public String getRunning()
	{
		JSONObject obj = new JSONObject();
		JSONArray list = new JSONArray();
		try
		{
			List<AID> aids = Global.getAgentManager().getRunning();
			if (!aids.isEmpty())
			{
				for (AID aid : aids)
					list.add(aid.toString());
				obj.put("running", list);
				return obj.toJSONString();
			}
		} catch (Exception e)
		{
			logger.log(Level.WARNING, "Error while loading running agents.", e);
		}
		obj.put("running", list);
		return obj.toJSONString();
	}

	@GET
	@Path("/start/{module}/{ejbName}/{runtimeName}")
	public String start(@PathParam("module") String module, @PathParam("ejbName") String ejbName,
			@PathParam("runtimeName") String runtimeName)
	{
		Serializable[] args = null; // arguments ??
		AID aid = new AID(module, ejbName, runtimeName);
		try
		{
			Global.getAgentManager().start(aid, args);
			return "{\"success\": true}";
		} catch (Exception e)
		{
			logger.log(Level.INFO, "Error while creating [" + aid + "]", e);
			return "{\"success\": false}";
		}
	}

	@GET
	@Path("/stop/{module}/{ejbName}/{runtimeName}")
	public String stop(@PathParam("module") String module, @PathParam("ejbName") String ejbName,
			@PathParam("runtimeName") String runtimeName)
	{
		AID aid = new AID(module, ejbName, runtimeName);
		try
		{
			Global.getAgentManager().stop(aid);
			return "{\"success\": true}";
		} catch (Exception e)
		{
			logger.log(Level.WARNING, "Stopping agent - [" + runtimeName + "] failed.", e);
			return "{\"success\": false}";
		}
	}
}
