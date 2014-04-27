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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import xjaf2x.server.Deployment;
import xjaf2x.server.Global;
import xjaf2x.server.agentmanager.agent.AID;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import xjaf2x.server.messagemanager.fipaacl.Performative;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;



/**
 *
 * @author <a href="rade.milovanovic@hotmail.com">Rade Milovanovic</a>
 */

@Path("/")
public class RESTws
{
	@SuppressWarnings("unchecked")
	@GET
	@Produces("application/json")
	@Path("/getfamilies")
	public String getFamilies()
	{		
		JSONObject obj = new JSONObject();
		JSONArray list = new JSONArray();
		List<String> families;
		try
		{
			families = Global.getAgentManager().getFamilies();
			for (String str : families)
				list.add(str);
		} catch (Exception e)
		{			
			e.printStackTrace();
		}		
		obj.put("families", list);
		return obj.toJSONString();
	}

	@SuppressWarnings("unchecked")
	@GET
	@Produces("application/json")
	@Path("/getrunning")
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
				{					
					list.add(aid.getFamily()+ "/" + aid.getRuntimeName());
				}		
				obj.put("running", list);
				return obj.toJSONString();
			} 
		} catch (Exception e)
		{			
			e.printStackTrace();
		}
		obj.put("running", list);
		return obj.toJSONString();
	}

	@GET
	@Path("/remove/{family}/{runtimeName}")
	public String deleteAgent(@PathParam("family") String family,
			@PathParam("runtimeName") String runtimeName)
	{
		AID aid = new AID(family, runtimeName);
		try
		{
			Global.getAgentManager().stop(aid);
			return "{\"success\": true}";
		} catch (Exception e)
		{
			return "{\"success\": false}";
		}
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Produces("application/json")
	@Path("/getperformatives")
	public String getPerformatives()
	{
		JSONObject obj = new JSONObject();
		JSONArray list = new JSONArray();			
		Performative[] performatives = Performative.values();		
		for (Performative p : performatives)		{
			
			list.add(p.toString());
		}
		obj.put("performatives", list);
		return obj.toJSONString();
	}

	
	@GET
	@Path("/create/{family}/{runtimeName}")
	public String createAgent(@PathParam("family") String family,
			@PathParam("runtimeName") String runtimeName)
	{
		Serializable[] args = null; // argumenti ??
		AID aid;
		try
		{
			aid = Global.getAgentManager().start(family, runtimeName, args);
			return "{\"success\": true}";
		} catch (Exception e)
		{
			return "{\"success\": false}";
		}

	}

	@GET
	@Produces("application/json")
	@Path("/sendquickmsg/{family}/{runtimeName}/{performative}/{content}")
	public String sendQuickMessage(@PathParam("family") String family,
			@PathParam("runtimeName") String runtimeName,
			@PathParam("performative") String performative, @PathParam("content") String content)
	{

		AID aid = new AID(family, runtimeName);
		Performative p = Performative.valueOf(performative);
		ACLMessage msg = new ACLMessage(p);
		msg.addReceiver(aid);
		msg.setContent(content);
		try
		{
			Global.getMessageManager().post(msg);
		} catch (Exception e)
		{
			return "{\"success\": false}";
		}

		return "{\"success\": true}";
	}
	
	
	@GET
	@Produces("application/json")
	@Path("/sendmsg/{performative}/{senderFam}/{senderName}/{recieverFam}/{recieverName}/{replyToFam}/{replyToName}/{content}/{language}/{encoding}/{ontology}/{protocol}/{conversationId}/{replyWith}/{replyBy}")
	public String sendMessage(@PathParam("performative") String performative,
						      @PathParam("senderFam") String senderFam,
						      @PathParam("senderName") String senderName,
						      @PathParam("recieverFam") String recieverFam,
						      @PathParam("recieverName") String recieverName,
						      @PathParam("replyToFam") String replyToFam,
						      @PathParam("replyToName") String replyToName,
						      @PathParam("content") String content,
						      @PathParam("language") String language,
						      @PathParam("encoding") String encoding,
						      @PathParam("ontology") String ontology,
						      @PathParam("protocol") String protocol,
						      @PathParam("conversationId") String conversationId,
						      @PathParam("replyWith") String replyWith,
						      @PathParam("replyBy") long replyBy)
	{

		
		Performative p = Performative.valueOf(performative);
		ACLMessage msg = new ACLMessage(p); 	
		AID sender = new AID(senderFam, senderName);
		msg.setSender(sender);
		AID reciever = new AID(recieverFam, recieverName);
		msg.addReceiver(reciever);
		AID replyTo = new AID(replyToFam, replyToName);
		msg.setReplyTo(replyTo);
		msg.setContent(content);
		msg.setLanguage(language);
		msg.setEncoding(encoding);
		msg.setOntology(ontology);
		msg.setProtocol(protocol);
		msg.setConversationId(conversationId);
		msg.setReplyWith(replyWith);
		msg.setReplyBy(replyBy);
		
		try
		{
			Global.getMessageManager().post(msg);
		} catch (Exception e)
		{
			return "{\"success\": false}";
		}

		return "{\"success\": true}";
	}
	
	
	@POST
	@Consumes("multipart/form-data")
	@Path("/deployagent/{masternodeaddress}/{applicationname}/{file}")
	public Response deployAgent(@MultipartForm MyMultipartForm form)
	{		
		String output;
		try
		{
			final String SERVER_UPLOAD_LOCATION_FOLDER = "tmp/";
			
			String fileName = SERVER_UPLOAD_LOCATION_FOLDER + form.getMasternodeaddress() + "_" + form.getApplicationname() + ".jar";
	
			saveFile(form.getFile_input(), fileName);
	
			output = "File saved to server location : " + fileName;
			
			File file = new File(fileName);
	
			
			Deployment deployment = new Deployment(form.getMasternodeaddress());
			deployment.deploy(form.getApplicationname(), file);
			
			return Response.status(200).entity(output).build();
		} catch (Exception e)
		{
			output = "ERROR!";
			return Response.status(400).entity(output).build();
		}

		
	}
	
	
	private void saveFile(InputStream uploadedInputStream, String serverLocation) {

		try {
			OutputStream outpuStream = new FileOutputStream(new File(
					serverLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			outpuStream = new FileOutputStream(new File(serverLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				outpuStream.write(bytes, 0, read);
			}
			outpuStream.flush();
			outpuStream.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
}
