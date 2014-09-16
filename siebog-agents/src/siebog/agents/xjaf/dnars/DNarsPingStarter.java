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

package siebog.agents.xjaf.dnars;

import java.io.IOException;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import siebog.SiebogCluster;
import siebog.agents.xjaf.Module;
import siebog.utils.ObjectFactory;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.AgentClass;
import siebog.xjaf.fipa.ACLMessage;
import siebog.xjaf.fipa.Performative;
import siebog.xjaf.managers.AgentInitArgs;

/**
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class DNarsPingStarter {
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException,
			NamingException {
		SiebogCluster.init();

		AgentInitArgs agArgs = new AgentInitArgs("domain->dnars");

		AgentClass agClass = new AgentClass(Module.NAME, "DNarsPing");

		AID aid = ObjectFactory.getAgentManager().startAgent(agClass, "dnars", agArgs);

		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.receivers.add(aid);
		ObjectFactory.getMessageManager().post(msg);
	}
}
