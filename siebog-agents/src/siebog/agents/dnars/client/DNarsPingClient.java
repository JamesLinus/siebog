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

package siebog.agents.dnars.client;

import java.io.IOException;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import siebog.server.xjaf.Global;
import siebog.server.xjaf.base.AID;
import siebog.server.xjaf.base.AgentClass;
import siebog.server.xjaf.fipa.acl.ACLMessage;
import siebog.server.xjaf.fipa.acl.Performative;
import siebog.server.xjaf.managers.AgentInitArgs;
import siebog.server.xjaf.utils.config.XjafCluster;

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class DNarsPingClient
{
	public static void main(String[] args) throws IOException, ParserConfigurationException,
			SAXException, NamingException
	{
		XjafCluster.init(true);
		
		AgentInitArgs agArgs = new AgentInitArgs("domain->dnars");
		
		AgentClass agClass = new AgentClass(Global.SERVER, "DNarsPing");
		
		AID aid = Global.getAgentManager().start(agClass, "dnars", agArgs);
		
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.addReceiver(aid);
		Global.getMessageManager().post(msg);
	}
}
