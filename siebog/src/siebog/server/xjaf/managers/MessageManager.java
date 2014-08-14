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

package siebog.server.xjaf.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.infinispan.Cache;
import siebog.server.xjaf.Global;
import siebog.server.xjaf.agents.base.AID;
import siebog.server.xjaf.agents.base.AgentI;
import siebog.server.xjaf.agents.fipa.acl.ACLMessage;
import siebog.server.xjaf.agents.fipa.acl.Performative;

/**
 * Default message manager implementation.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 * @author <a href="rade.milovanovic@hotmail.com">Rade Milovanovic</a>
 */
@Stateless
@Remote(MessageManagerI.class)
@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@LocalBean
public class MessageManager implements MessageManagerI
{
	private static final Logger logger = Logger.getLogger(MessageManager.class.getName());
	private Cache<AID, AgentI> runningAgents;
	
	@PostConstruct
	public void postConstruct()
	{
		try
		{	
			runningAgents = Global.getRunningAgents();
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "MessageManager initialization error.", ex);
		}
	}
	
	@GET
	@Path("/performatives")
	public List<String> getPerformatives()
	{
		final Performative[] arr = Performative.values();
		List<String> list = new ArrayList<>(arr.length);
		for (Performative p: arr)
			list.add(p.toString());
		return list;		
	}

	@Override
	public void post(ACLMessage message)
	{
		for (AID aid : message.getReceivers())
		{
			if (aid == null)
				continue;
			AgentI agent = runningAgents.get(aid);
			if (agent != null)
				agent.handleMessage(message);
			else
				logger.info("Agent not running: [" + aid + "]");
		}
	}
	
	@GET
	@Produces("application/json")
	@Path("/postquickmsg/{agents}/{performative}/{content}")
	public String sendQuickMessage(@PathParam("agents") String agents,
			@PathParam("performative") String performative, @PathParam("content") String content)
	{

		Set<AID> receivers = new HashSet<AID>();
		Performative p = Performative.valueOf(performative);
		ACLMessage msg = new ACLMessage(p);
		msg.setContent(content);
		String[] allAgents = agents.split(",");
		for (String agent : allAgents)
		{
			String[] parts = agent.split("@");
			String module = parts[0];
			String ejbName = parts[1];
			String runtimeName = parts[2];
			AID aid = new AID(module, ejbName, runtimeName);
			receivers.add(aid);
		}
		msg.setReceivers(receivers);
		try
		{
			Global.getMessageManager().post(msg);
		} catch (Exception e)
		{
			logger.log(Level.INFO, "Error while sending message.", e);
			return "{\"success\": false}";
		}

		return "{\"success\": true}";
	}

	@POST
	@Produces("application/json")
	@Path("/postmsg")
	public String sendMessage(String post)
	{
		String[] postArray = post.split("&");
		List<String> list = new ArrayList<>();
		for (String params : postArray)
		{
			String[] param = params.split("=");
			if (param.length == 1)
			{
				list.add("");
			} else
			{
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
		Set<AID> receivers = new HashSet<AID>();
		String[] allAgents = recievers.split("%2C");
		for (String agent : allAgents)
		{
			String[] parts = agent.split("%40");
			String module = parts[0];
			String ejbName = parts[1];
			String runtimeName = parts[2];
			AID aid = new AID(module, ejbName, runtimeName);
			receivers.add(aid);
		}
		msg.setReceivers(receivers);
		String[] replyParts = replyToAgent.split("%2F");
		AID replyTo = new AID(replyParts[0], replyParts[1], replyParts[2]);
		msg.setReplyTo(replyTo);
		msg.setContent(content);
		msg.setLanguage(language);
		msg.setEncoding(encoding);
		msg.setOntology(ontology);
		msg.setProtocol(protocol);
		msg.setConversationId(conversationId);
		msg.setReplyWith(replyWith);
		msg.setReplyBy(Long.valueOf(replyBy));
		try
		{
			Global.getMessageManager().post(msg);
		} catch (Exception e)
		{
			logger.log(Level.INFO, "Error while sending message.", e);
			return "{\"success\": false}";
		}

		return "{\"success\": true}";
	}

	@Override
	public String ping()
	{
		return "Pong from " + System.getProperty("jboss.node.name");
	}
}
