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

package siebog;

import java.util.Properties;
import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;
import siebog.core.Global;
import siebog.core.config.NodeConfig;

/**
 * Helper class for global cluster initialization.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class SiebogClient {
	private static boolean connected;

	public static void connect(String masterAddress) {
		if (connected)
			return;
		if (masterAddress == null)
			masterAddress = NodeConfig.get().getAddress();

		Properties p = new Properties();
		p.put("endpoint.name", "client-endpoint");
		// p.put("deployment.node.selector", RRDeploymentNodeSelector.class.getName());

		p.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
		p.put("remote.clusters", "ejb");
		p.put("remote.cluster.ejb.username", Global.USERNAME);
		p.put("remote.cluster.ejb.password", Global.PASSWORD);
		// p.put("remote.cluster.ejb.clusternode.selector", RRClusterNodeSelector.class.getName());

		final String conn = "master";
		p.put("remote.connections", conn);
		final String prefix = "remote.connection." + conn;
		p.put(prefix + ".host", masterAddress);
		p.put(prefix + ".port", "8080");
		p.put(prefix + ".connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
		p.put(prefix + ".username", Global.USERNAME);
		p.put(prefix + ".password", Global.PASSWORD);

		EJBClientConfiguration cc = new PropertiesBasedEJBClientConfiguration(p);
		ContextSelector<EJBClientContext> selector = new ConfigBasedEJBClientContextSelector(cc);
		EJBClientContext.setSelector(selector);
		connected = true;
	}
}
