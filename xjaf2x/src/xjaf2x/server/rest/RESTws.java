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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import xjaf2x.Global;
import xjaf2x.server.Deployment;
import xjaf2x.server.agentmanager.AID;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import xjaf2x.server.messagemanager.fipaacl.Performative;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

/**
 * 
 * @author <a href="rade.milovanovic@hotmail.com">Rade Milovanovic</a>
 */

@Path("/")
public class RESTws {
	
	private static final Logger logger = Logger.getLogger(RESTws.class.getName());
	
	@SuppressWarnings("unchecked")
	@GET
	@Produces("application/json")
	@Path("/getfamilies")
	public String getFamilies() {
		JSONObject obj = new JSONObject();
		JSONArray list = new JSONArray();
		try {
			List<AID> deployed = Global.getAgentManager().getDeployed();
			for (AID aid : deployed)
				list.add(aid.toString());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error while loading deployed agents.", e);			
		}
		obj.put("families", list);
		return obj.toJSONString();
	}

	@SuppressWarnings("unchecked")
	@GET
	@Produces("application/json")
	@Path("/getrunning")
	public String getRunning() {
		JSONObject obj = new JSONObject();
		JSONArray list = new JSONArray();
		try {
			List<AID> aids = Global.getAgentManager().getRunning();
			if (!aids.isEmpty()) {
				for (AID aid : aids)
					list.add(aid.toString());
				obj.put("running", list);
				return obj.toJSONString();
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error while loading running agents.", e);	
		}
		obj.put("running", list);
		return obj.toJSONString();
	}

	@GET
	@Path("/remove/{module}/{ejbName}/{runtimeName}")
	public String deleteAgent(@PathParam("module") String module,
			@PathParam("ejbName") String ejbName,
			@PathParam("runtimeName") String runtimeName) {
		AID aid = new AID(module, ejbName, runtimeName);
		try {
			Global.getAgentManager().stop(aid);
			return "{\"success\": true}";
		} catch (Exception e) {
			logger.log(Level.WARNING, "Stopping agent - ["+ runtimeName +"] failed.", e);	
			return "{\"success\": false}";
		}
	}

	@SuppressWarnings("unchecked")
	@GET
	@Produces("application/json")
	@Path("/getperformatives")
	public String getPerformatives() {
		JSONObject obj = new JSONObject();
		JSONArray list = new JSONArray();
		Performative[] performatives = Performative.values();
		for (Performative p : performatives)
			list.add(p.toString());
		obj.put("performatives", list);
		return obj.toJSONString();
	}

	@GET
	@Path("/create/{module}/{ejbName}/{runtimeName}")
	public String createAgent(@PathParam("module") String module,
			@PathParam("ejbName") String ejbName,
			@PathParam("runtimeName") String runtimeName) {
		Serializable[] args = null; // arguments ??
		AID aid = new AID(module, ejbName, runtimeName);
		try {			
			Global.getAgentManager().start(aid, args);
			return "{\"success\": true}";
		} catch (Exception e) {
			logger.log(Level.INFO, "Error while creating [" + aid + "]", e);
			return "{\"success\": false}";
		}
	}

	@GET
	@Produces("application/json")
	@Path("/sendquickmsg/{agents}/{performative}/{content}")
	public String sendQuickMessage(@PathParam("agents") String agents,
			@PathParam("performative") String performative,
			@PathParam("content") String content) {	
		
		Set<AID> receivers = new HashSet<AID>();
		Performative p = Performative.valueOf(performative);
		ACLMessage msg = new ACLMessage(p);
		msg.setContent(content);
		String[] allAgents = agents.split(",");
		for (String agent : allAgents) {
			String[] parts = agent.split("@");
			String module = parts[0];
			String ejbName = parts[1];
			String runtimeName = parts[2];
			AID aid = new AID(module, ejbName, runtimeName);
			receivers.add(aid);
		}
		msg.setReceivers(receivers);
		try {
			Global.getMessageManager().post(msg);
		} catch (Exception e) {
			logger.log(Level.INFO, "Error while sending message.", e);
			return "{\"success\": false}";
		}

		return "{\"success\": true}";
	}

	@POST
	@Produces("application/json")
	@Path("/sendmsg")
	public String sendMessage(String post)
	{
		String[] postArray = post.split("&");	
		List<String> list = new ArrayList<>();
		for(String params: postArray){			
			String[] param = params.split("=");
			if(param.length == 1){
				list.add("");	
			}else{
				list.add(param[1]);	
			}					
		}
		String performative = list.get(0); 
		String senderAgent = list.get(1);
		String recievers = list.get(2);
		String replyToAgent = list.get(3);
		String content = list.get(4);
		String language = list.get(5);
		String encoding = list.get(6);
		String ontology = list.get(7);
		String protocol = list.get(8);
		String conversationId = list.get(9);
		String replyWith = list.get(10);
		String replyBy = list.get(11);
		
		Performative p = Performative.valueOf(performative);		
		ACLMessage msg = new ACLMessage(p);
		// TODO : module, ejbName, runtimeName
		String[] sparts = senderAgent.split("%2F");
		AID sender = new AID(sparts[0], sparts[1], sparts[2]); // module,ejbName,runtimeName
		msg.setSender(sender);
		// TODO : module, ejbName, runtimeName
		Set<AID> receivers = new HashSet<AID>();
		String[] allAgents = recievers.split("%2C");
		for (String agent : allAgents) {
			String[] parts = agent.split("%40");
			String module = parts[0];
			String ejbName = parts[1];
			String runtimeName = parts[2];
			AID aid = new AID(module, ejbName, runtimeName);
			receivers.add(aid);
		}
		msg.setReceivers(receivers);
		// TODO : module, ejbName, runtimeName
		String[] replyParts = replyToAgent.split("%2F");
		AID replyTo = new AID(replyParts[0], replyParts[1], replyParts[2]);
		msg.setReplyTo(replyTo);
		msg.setContent(content);	
		if(!language.equals("")){
			msg.setLanguage(language);
		}
		if(!encoding.equals("")){
			msg.setEncoding(encoding);			
		}
		if(!ontology.equals("")){
			msg.setOntology(ontology);
		}
		if(!protocol.equals("")){
			msg.setProtocol(protocol);
		}
		if(!conversationId.equals("")){
			msg.setConversationId(conversationId);
		}
		if(!replyWith.equals("")){
			msg.setReplyWith(replyWith);
		}
		if(!replyBy.equals("")){
			msg.setReplyBy(Long.valueOf(replyBy));
		}		
		try {
			Global.getMessageManager().post(msg);
		} catch (Exception e) {
			logger.log(Level.INFO, "Error while sending message.", e);
			return "{\"success\": false}";
		}

		return "{\"success\": true}";
	}

	@POST
	@Consumes("multipart/form-data")
	@Path("/deployagent")
	public Response deployAgent(@MultipartForm MyMultipartForm form) {
		String output;
		try {
			URL location = RESTws.class.getProtectionDomain().getCodeSource()
					.getLocation();
			System.out.println(location.getFile());
			String folderurl = location.toString().substring(5) + "/tmp/";
			String fileName = folderurl + form.getMasternodeaddress() + "_"
					+ form.getApplicationname() + ".jar";
			saveFile(form.getFile_input(), folderurl, fileName);
			output = "File saved to server location : " + fileName;
			File file = new File(fileName);
			Deployment deployment = new Deployment(form.getMasternodeaddress());
			deployment.deploy(form.getApplicationname(), file);
			return Response.status(200).entity(output).build();
		} catch (Exception e) {
			output = "Error";
			logger.log(Level.INFO, "Error while deploying agent.", e);
			return Response.status(400).entity(output).build();
		}

	}

	private void saveFile(InputStream uploadedInputStream, String folderurl,
			String serverLocation) {

		try {
			new File(folderurl).mkdirs();

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
			logger.log(Level.INFO, "Error while saving file - [" + folderurl + "] .", e);			
		}
	}

}
