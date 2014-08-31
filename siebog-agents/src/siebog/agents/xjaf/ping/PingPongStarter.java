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

package siebog.agents.xjaf.ping;

import java.io.IOException;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import siebog.SiebogCluster;
import siebog.agents.xjaf.Module;
import siebog.utils.ManagerFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.fipa.Performative;
import siebog.xjaf.managers.AgentManager;
import siebog.xjaf.managers.MessageManager;

/**
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class PingPongStarter {
	/**
	 * @param args
	 * @throws NamingException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException,
			NamingException {
		SiebogCluster.init();
		final AgentManager agm = ManagerFactory.getAgentManager();
		AID ping = agm.startAgent(new AgentClass(Module.NAME, "Ping"), "Ping", null);
		agm.startAgent(new AgentClass(Module.NAME, "Pong"), "Pong", null);

		MessageManager msm = ManagerFactory.getMessageManager();
		msm.post(null, ping, Performative.REQUEST, "Pong");
	}

}
