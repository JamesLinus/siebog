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

package siebog.starter;

import java.io.File;
import java.io.IOException;
import siebog.starter.FileUtils;
import siebog.starter.config.NodeConfig;

/**
 * Global constants and utility functions.
 *
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public abstract class Global {
	public static final String VERSION = "1.2.1";

	public static final String // @formatter:off 
		GROUP = "xjaf2x-group",
		USERNAME = "xjaf2xadmin",
		PASSWORD = "xjaf2xpass~",
		SIEBOG_MODULE = "siebog", 
		MASTER_NAME = "xjaf-master"; // @formatter:on

	public static void printVersion() {
		System.out.println("-------------------------------------------------------------");
		System.out.println("Siebog Multiagent Framework v" + VERSION);
		System.out.println("-------------------------------------------------------------");
	}

	public static void checkServerVersion() {
		File f = new File(NodeConfig.get().getJBossHome(), "siebog.version");
		try {
			String stored = FileUtils.read(f);
			if (!VERSION.equals(stored)) {
				String msg = String.format("Version mismatch, expected %s, got %s.", VERSION,
						stored);
				throw new IllegalStateException(msg);
			}
		} catch (IOException ex) {
			throw new IllegalStateException(ex.getMessage());
		}
	}

	public static String getNodeName() {
		return System.getProperty("jboss.node.name");
	}
}
