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

package siebog.agents.jason.ha;

import jason.mas2j.parser.ParseException;
import java.io.File;
import java.io.IOException;
import siebog.SiebogClient;
import siebog.jasonee.JasonEEProject;
import siebog.utils.ObjectFactory;

/**
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class HAClient {
	public static void main(String[] args) throws IOException, ParseException {
		JasonEEProject p = JasonEEProject.loadFromFile(new File(
				"/home/dejan/dev/siebog/siebog-agents/high_availability.mas2j"));
		SiebogClient.connect("192.168.213.1");
		ObjectFactory.getJasonEEStarter().start(p);
	}
}
