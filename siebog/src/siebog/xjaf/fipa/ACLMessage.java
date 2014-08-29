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

package siebog.xjaf.fipa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.FormParam;
import org.hornetq.utils.json.JSONException;
import org.hornetq.utils.json.JSONObject;
import siebog.xjaf.core.AID;

/**
 * Represents a FIPA ACL message. Refer to <a
 * href="http://www.fipa.org/specs/fipa00061/SC00061G.pdf">FIPA ACL Message Structure
 * Specification</a> for more details.
 * 
 * @author <a href="tntvteod@neobee.net">Teodor-Najdan Trifunov</a>
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class ACLMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	// Denotes the type of the communicative act of the ACL message.
	@FormParam("performative")
	private Performative performative;

	/* Participants in Communication */

	// Denotes the identity of the sender of the message.
	@FormParam("sender")
	private AID sender;
	// Denotes the identity of the intended recipients of the message.
	@FormParam("receivers")
	private List<AID> receivers;
	// This parameter indicates that subsequent messages in this conversation
	// thread are to be directed to the agent named in the reply-to parameter,
	// instead of to the agent named in the sender parameter.
	@FormParam("replyTo")
	private AID replyTo;

	/* Description of Content */

	// Denotes the content of the message; equivalently denotes the
	// object of the action.
	@FormParam("content")
	private String content;
	// Denotes the language in which the content parameter is expressed.
	@FormParam("language")
	private String language;
	// Denotes the specific encoding of the content language expression.
	@FormParam("encoding")
	private String encoding;
	// Denotes the ontology(s) used to give a meaning to the symbols in
	// the content expression.
	@FormParam("ontology")
	private String ontology;

	/* Control of Conversation */

	// Denotes the interaction protocol that the sending agent is
	// employing with this ACL message.
	@FormParam("protocol")
	private String protocol;
	// Introduces an expression (a conversation identifier) which is used
	// to identify the ongoing sequence of communicative acts that
	// together form a conversation.
	@FormParam("conversationId")
	private String conversationId;
	// Introduces an expression that will be used by the responding
	// agent to identify this message.
	@FormParam("replyWith")
	private String replyWith;
	// Denotes an expression that references an earlier action to which
	// this message is a reply.
	@FormParam("inReplyTo")
	private String inReplyTo;
	// Denotes a time and/or date expression which indicates the latest
	// time by which the sending agent would like to receive a reply.
	@FormParam("replyBy")
	private long replyBy;

	public ACLMessage() {
		this(Performative.NOT_UNDERSTOOD);
	}

	public ACLMessage(Performative performative) {
		this.performative = performative;
		receivers = new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	public ACLMessage(String jsonString) throws JSONException {
		JSONObject obj = new JSONObject(jsonString);
		performative = (Performative) obj.get("performative");
		sender = (AID) obj.get("sender");
		receivers = (List<AID>) obj.get("receivers");
		replyTo = (AID) obj.get("replyTo");
		content = obj.getString("content");
		language = obj.getString("language");
		encoding = obj.getString("encoding");
		ontology = obj.getString("ontology");
		protocol = obj.getString("protocol");
		conversationId = obj.getString("conversationId");
		replyWith = obj.getString("replyWith");
		inReplyTo = obj.getString("inReplyTo");
		replyBy = obj.getLong("replyBy");
	}

	public Performative getPerformative() {
		return performative;
	}

	public void setPerformative(Performative performative) {
		this.performative = performative;
	}

	public ACLMessage makeReply() {
		return makeReply(performative);
	}

	public ACLMessage makeReply(Performative performative) {
		ACLMessage reply = new ACLMessage(performative);
		// receiver
		reply.addReceiver(replyTo != null ? replyTo : sender);
		// description of content
		reply.setLanguage(language);
		reply.setOntology(ontology);
		reply.setEncoding(encoding);
		// control of conversation
		reply.setProtocol(protocol);
		reply.setConversationId(conversationId);
		reply.setInReplyTo(replyWith);
		return reply;
	}

	public AID getSender() {
		return sender;
	}

	public void setSender(AID sender) {
		this.sender = sender;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getOntology() {
		return ontology;
	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	public String getReplyWith() {
		return replyWith;
	}

	public void setReplyWith(String replyWith) {
		this.replyWith = replyWith;
	}

	public String getInReplyTo() {
		return inReplyTo;
	}

	public void setInReplyTo(String inReplyTo) {
		this.inReplyTo = inReplyTo;
	}

	public long getReplyBy() {
		return replyBy;
	}

	public void setReplyBy(long replyBy) {
		this.replyBy = replyBy;
	}

	public AID getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(AID replyTo) {
		this.replyTo = replyTo;
	}

	public List<AID> getReceivers() {
		return receivers;
	}

	public void setReceivers(List<AID> receivers) {
		this.receivers = receivers;
	}

	public void addReceiver(AID receiver) {
		receivers.add(receiver);
	}

	public void removeReceiver(AID receiver) {
		receivers.remove(receiver);
	}

	public void clearReceivers() {
		receivers.clear();
	}

	@Override
	public String toString() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("performative", performative);
			obj.put("sender", sender);
			obj.put("receivers", receivers);
			obj.put("replyTo", replyTo);
			obj.put("content", content);
			obj.put("language", language);
			obj.put("encoding", encoding);
			obj.put("ontology", ontology);
			obj.put("protocol", protocol);
			obj.put("conversationId", conversationId);
			obj.put("replyWith", replyWith);
			obj.put("inReplyTo", inReplyTo);
			obj.put("replyBy", replyBy);
		} catch (JSONException ex) {
		}
		return obj.toString();
	}
}