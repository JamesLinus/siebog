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

package siebog.agents.jasonee.cnet;

import java.io.File;
import java.net.URISyntaxException;
import siebog.SiebogClient;
import siebog.jasonee.JasonEEProject;
import siebog.utils.ObjectFactory;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class CNetClient {

	public static void main(String[] args) throws URISyntaxException {
		File f = new File(CNetClient.class.getResource("cnet.mas2j").toURI());
		JasonEEProject p = JasonEEProject.loadFromFile(f);
		SiebogClient.connect("192.168.213.1", "192.168.213.129");
		// "192.168.213.1", "192.168.213.129"
		// "172.16.249.1", "172.16.249.129"
		// "192.168.124.1", "192.168.124.129", "192.168.124.130"
		ObjectFactory.getJasonEEStarter().start(p);
	}

}
