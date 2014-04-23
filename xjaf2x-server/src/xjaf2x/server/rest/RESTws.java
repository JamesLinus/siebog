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

package xjaf2x.server.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.Serializable;
import java.util.List;
import xjaf2x.server.Global;
import xjaf2x.server.agentmanager.agent.AID;

/**
 *
 * @author <a href="rade.milovanovic@hotmail.com">Rade Milovanovic</a>
 */

@Path("/rest")
public class RESTws {

	@GET
	@Produces("application/json")
	@Path("/getfamilies")
	public String getFamilies()
	{
		
		StringBuilder lista = new StringBuilder();
		lista.append("{\"families\":[");
		List<String> families;		
		try {
			families =  Global.getAgentManager().getFamilies();
			for (String str : families)
				lista.append("\"" + str + "\",");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		int i = lista.lastIndexOf(",");
		int k = lista.length();
		lista.replace(i,k, "]}");
		return lista.toString();
	}
	
	
	@GET
	@Produces("application/json")
	@Path("/getrunning")
	public String getRunning(){
		StringBuilder lista = new StringBuilder();
		lista.append("\"running agents\":[");
		try {
			List<AID> aids = Global.getAgentManager().getRunning();		
			if(!aids.isEmpty()){
				for (AID aid : aids){
					lista.append("\"" + aid.getFamily() + "/" + aid.getRuntimeName() + "\", ");
				}
				int i = lista.lastIndexOf(",");
				int k = lista.length();
				lista.replace(i,k, "]");
				return lista.toString();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return "";
	}
	
	
	@DELETE
	@Path("/remove/{family}/{runtimeName}")		
	public void deleteAgent(@PathParam("family") String family, @PathParam("runtimeName") String runtimeName) {		    
        AID aid = new AID(family, runtimeName);		
        try {
			Global.getAgentManager().stop(aid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
    }
	
	@POST
	@Path("/create/{family}/{runtimeName}")		
    public void createAgent(@PathParam("family") String family, @PathParam("runtimeName") String runtimeName) {        
		Serializable[] args = null; //argumenti ??
		AID aid;
		try {
			aid = Global.getAgentManager().start(family,runtimeName, args);		
			if(aid != null){
				//throw new WebApplicationException(Response.Status.CREATED);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
	
	
}
