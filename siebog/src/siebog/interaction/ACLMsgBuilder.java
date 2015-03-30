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

package siebog.interaction;

import java.io.Serializable;
import java.util.Arrays;
import siebog.agents.AID;

/**
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class ACLMsgBuilder {
	private ACLMessage msg;

	private ACLMsgBuilder(Performative p) {
		msg = new ACLMessage(p);
	}

	public static ACLMsgBuilder performative(Performative p) {
		return new ACLMsgBuilder(p);
	}

	public ACLMsgBuilder sender(AID sender) {
		msg.sender = sender;
		return this;
	}

	public ACLMsgBuilder receivers(AID... receivers) {
		msg.receivers.addAll(Arrays.asList(receivers));
		return this;
	}

	public ACLMsgBuilder replyTo(AID replyTo) {
		msg.replyTo = replyTo;
		return this;
	}

	public ACLMsgBuilder content(String content) {
		msg.content = content;
		return this;
	}

	public ACLMsgBuilder contentObj(Serializable contentObj) {
		msg.contentObj = contentObj;
		return this;
	}

	public ACLMsgBuilder userArg(String key, Serializable value) {
		msg.userArgs.put(key, value);
		return this;
	}

	public ACLMsgBuilder language(String language) {
		msg.language = language;
		return this;
	}

	public ACLMsgBuilder encoding(String encoding) {
		msg.encoding = encoding;
		return this;
	}

	public ACLMsgBuilder ontology(String ontology) {
		msg.ontology = ontology;
		return this;
	}

	public ACLMsgBuilder protocol(String protocol) {
		msg.protocol = protocol;
		return this;
	}

	public ACLMsgBuilder conversationId(String conversationId) {
		msg.conversationId = conversationId;
		return this;
	}

	public ACLMsgBuilder replyWith(String replyWith) {
		msg.replyWith = replyWith;
		return this;
	}

	public ACLMsgBuilder inReplyTo(String inReplyTo) {
		msg.inReplyTo = inReplyTo;
		return this;
	}

	public ACLMsgBuilder replyBy(long replyBy) {
		msg.replyBy = replyBy;
		return this;
	}

	public ACLMessage build() {
		return msg;
	}
}
