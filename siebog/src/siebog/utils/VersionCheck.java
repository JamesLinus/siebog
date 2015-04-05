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

package siebog.utils;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.core.Global;
import siebog.core.Setup;

/**
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Singleton
@Startup
@LocalBean
public class VersionCheck {
	private static final Logger LOG = LoggerFactory.getLogger(VersionCheck.class);

	@PostConstruct
	public void postConstruct() {
		try {
			Global.checkServerVersion();
		} catch (IllegalStateException ex) {
			String msg = "\n---------------------------------------------------------------------------\n"
					+ "Hey there! You are using outdated settings of the WildFly server.\n"
					+ "Please stop the server, go to the Siebog installation folder, and execute:\n"
					+ "\n\t\tjava -jar start.jar --setup\n\n"
					+ "Or, if you're inside an IDE, run the "
					+ Setup.class.getName()
					+ " application inside \n"
					+ "the 'starter' project.\n\n"
					+ "An exception will be thrown now. Have a nice day! :)\n"
					+ "---------------------------------------------------------------------------\n";
			LOG.error(msg);
			throw ex;
		}
	}
}
