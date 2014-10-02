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
 * 
 * Based on JADE infrastructure for Jason 1.4.1, 
 * jason.infra.jade.JadeAg and jason.infra.jade.JasonBridgeArch
 * Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
 * 
 * To contact the original authors:
 * http://www.inf.ufrgs.br/~bordini
 * http://www.das.ufsc.br/~jomi
 */

package siebog.jasonee;

import java.io.Serializable;
import jason.asSemantics.Message;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import siebog.xjaf.core.AID;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;

/**
 * Transformations between FIPA ACL and KQML messages.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class JasonMessage {
	public static ACLMessage toAclMessage(Message jmsg) {
		ACLMessage acl = createForIlForce(jmsg.getIlForce());
		acl.sender = new AID(jmsg.getSender());
		final String jmsgReceiver = jmsg.getReceiver();
		if (jmsgReceiver != null)
			acl.receivers.add(new AID(jmsgReceiver));
		if (jmsg.getPropCont() instanceof Term || jmsg.getPropCont() instanceof String) {
			acl.content = jmsg.getPropCont().toString();
		} else {
			acl.contentObj = (Serializable) jmsg.getPropCont();
		}
		acl.replyWith = jmsg.getMsgId();
		acl.language = "AgentSpeak";
		if (jmsg.getInReplyTo() != null)
			acl.inReplyTo = jmsg.getInReplyTo();
		return acl;
	}

	public static String getIlForce(ACLMessage acl) {
		switch (acl.performative) {
		case INFORM:
			return "tell";
		case QUERY_REF:
			return "askOne";
		case REQUEST:
			return "achieve";
		case INFORM_REF:
			String kp = (String) acl.userArgs.get("kqml-performative");
			if (kp != null)
				return kp;
			break;
		default: // ignore compiler warning
		}
		return acl.performative.toString().toLowerCase().replace('-', '_');
	}

	public static Serializable getJasonContent(ACLMessage acl) {
		Serializable content = acl.contentObj;
		if (content != null && content instanceof String)
			try {
				content = ASSyntax.parseTerm((String) content);
			} catch (Exception e) {
			}

		if (content == null && acl.content != null)
			try {
				content = ASSyntax.parseTerm(acl.content);
			} catch (Exception e) {
				content = new StringTermImpl(acl.content);
			}

		return content;
	}

	private static ACLMessage createForIlForce(String ilForce) {
		switch (ilForce) {
		case "tell":
			return new ACLMessage(Performative.INFORM);
		case "askOne":
			return new ACLMessage(Performative.QUERY_REF);
		case "achieve":
			return new ACLMessage(Performative.REQUEST);
		case "untell":
		case "unachieve":
		case "askAll":
		case "askHow":
		case "tellHow":
		case "untellHow":
			ACLMessage acl = new ACLMessage(Performative.INFORM_REF);
			acl.userArgs.put("kqml-performative", ilForce);
			return acl;
		default:
			Performative p = Performative.valueOf(ilForce.toUpperCase());
			return new ACLMessage(p);
		}
	}

}
