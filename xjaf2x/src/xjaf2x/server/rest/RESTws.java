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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
			e.printStackTrace();
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
			e.printStackTrace();
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
		try {
			AID aid = new AID(module, ejbName, runtimeName);
			Global.getAgentManager().start(aid, args);
			return "{\"success\": true}";
		} catch (Exception e) {
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
			String[] parts = agent.split("^");
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
			return "{\"success\": false}";
		}

		return "{\"success\": true}";
	}

	@GET
	@Produces("application/json")
	@Path("/sendmsg/{performative}/{senderAgent}/{recievers}/{replyToAgent}/{content}/{language}/{encoding}/{ontology}/{protocol}/{conversationId}/{replyWith}/{replyBy}")
	public String sendMessage(@PathParam("performative") String performative,
			@PathParam("senderAgent") String senderAgent,
			@PathParam("recievers") String recievers,
			@PathParam("replyToAgent") String replyToAgent,
			@PathParam("content") String content,
			@PathParam("language") String language,
			@PathParam("encoding") String encoding,
			@PathParam("ontology") String ontology,
			@PathParam("protocol") String protocol,
			@PathParam("conversationId") String conversationId,
			@PathParam("replyWith") String replyWith,
			@PathParam("replyBy") long replyBy) {

		Performative p = Performative.valueOf(performative);
		ACLMessage msg = new ACLMessage(p);
		// TODO : module, ejbName, runtimeName
		String[] sparts = senderAgent.split("^");
		AID sender = new AID(sparts[0], sparts[1], sparts[2]); // module,ejbName,runtimeName
		msg.setSender(sender);
		// TODO : module, ejbName, runtimeName
		Set<AID> receivers = new HashSet<AID>();
		String[] allAgents = recievers.split(",");
		for (String agent : allAgents) {
			String[] parts = agent.split("^");
			String module = parts[0];
			String ejbName = parts[1];
			String runtimeName = parts[2];
			AID aid = new AID(module, ejbName, runtimeName);
			receivers.add(aid);
		}
		msg.setReceivers(receivers);
		// TODO : module, ejbName, runtimeName
		String[] replyParts = replyToAgent.split("^");
		AID replyTo = new AID(replyParts[0], replyParts[1], replyParts[2]);
		msg.setReplyTo(replyTo);
		msg.setContent(content);
		msg.setLanguage(language);
		msg.setEncoding(encoding);
		msg.setOntology(ontology);
		msg.setProtocol(protocol);
		msg.setConversationId(conversationId);
		msg.setReplyWith(replyWith);
		msg.setReplyBy(replyBy);

		try {
			Global.getMessageManager().post(msg);
		} catch (Exception e) {
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

			e.printStackTrace();
		}
	}

}
