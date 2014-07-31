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

package xjaf.agents.ping.client;

import java.io.IOException;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import xjaf.server.Global;
import xjaf.server.agm.AID;
import xjaf.server.msm.fipa.acl.ACLMessage;
import xjaf.server.msm.fipa.acl.Performative;
import xjaf.server.utils.config.XjafCluster;

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
		AID ping = new AID(Global.SERVER, "Ping", "Ping");
		Global.getAgentManager().start(ping);
		Global.getAgentManager().start(new AID(Global.SERVER, "Pong", "Pong"));
		
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.addReceiver(ping);
		msg.setContent("Pong");
		Global.getMessageManager().post(msg);
	}

}
