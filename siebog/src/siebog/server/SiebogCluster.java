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

package siebog.server;

import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;
import siebog.server.config.NodeConfig;
import siebog.server.config.RRClusterNodeSelector;
import siebog.server.config.RRDeploymentNodeSelector;
import siebog.server.xjaf.Global;

/**
 * Helper class for global cluster initialization.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class SiebogCluster {
	private static final Logger logger = Logger.getLogger(SiebogCluster.class.getName());
	private static NodeConfig config;
	private static boolean initialized;

	public static NodeConfig getConfig() {
		if (config == null) {
			try {
				// TODO if file not found, use default settings
				config = new NodeConfig();
			} catch (Exception ex) {
				throw new IllegalStateException("Unable to initialize " + SiebogCluster.class.getSimpleName(), ex);
			}
		}
		return config;
	}

	public static synchronized void init() {
		if (!initialized) {
			final Set<String> clusterNodes = getConfig().getClusterNodes();
			if (clusterNodes == null || clusterNodes.size() == 0) {
				logger.warning("Trying to initialize without specifying cluster nodes.");
				return;
			}

			Properties p = new Properties();
			p.put("endpoint.name", "client-endpoint");
			p.put("deployment.node.selector", RRDeploymentNodeSelector.class.getName());

			p.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
			p.put("remote.clusters", "ejb");
			p.put("remote.cluster.ejb.username", Global.USERNAME);
			p.put("remote.cluster.ejb.password", Global.PASSWORD);
			p.put("remote.cluster.ejb.clusternode.selector", RRClusterNodeSelector.class.getName());

			StringBuilder connections = new StringBuilder();
			String sep = "";
			for (String str : clusterNodes) {
				String addr = "C_" + str.replace('.', '_');
				final String id = "remote.connection." + addr;

				p.put(id + ".host", str);
				p.put(id + ".port", "8080");
				p.put(id + ".connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
				p.put(id + ".username", Global.USERNAME);
				p.put(id + ".password", Global.PASSWORD);

				connections.append(sep).append(addr);
				if (sep.length() == 0)
					sep = ",";
			}

			p.put("remote.connections", connections.toString());

			EJBClientConfiguration cc = new PropertiesBasedEJBClientConfiguration(p);
			ContextSelector<EJBClientContext> selector = new ConfigBasedEJBClientContextSelector(cc);
			EJBClientContext.setSelector(selector);

			initialized = true;
			logger.info("Initialized EJB client context with cluster nodes " + clusterNodes);
		}
	}
}
