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

package agents.ping.client;

import java.io.IOException;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import siebog.server.xjaf.Global;
import siebog.server.xjaf.agents.base.AID;
import siebog.server.xjaf.agents.base.AgentClass;
import siebog.server.xjaf.agents.fipa.acl.ACLMessage;
import siebog.server.xjaf.agents.fipa.acl.Performative;
import siebog.server.xjaf.managers.AgentManagerI;
import siebog.server.xjaf.utils.config.XjafCluster;

/**
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class PingPongStarter
{

	/**
	 * @param args
	 * @throws NamingException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, NamingException
	{
		XjafCluster.init(true);
		final AgentManagerI agm = Global.getAgentManager();
		AID ping = agm.start(new AgentClass(Global.SERVER, "Ping"), "Ping", null);
		agm.start(new AgentClass(Global.SERVER, "Pong"), "Pong", null);
		
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.addReceiver(ping);
		msg.setContent("Pong");
		Global.getMessageManager().post(msg);
	}

}
