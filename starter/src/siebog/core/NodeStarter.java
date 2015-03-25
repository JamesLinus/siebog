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

package siebog.core;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.logging.Logger;
import org.jboss.as.controller.client.helpers.domain.DomainClient;
import org.jboss.as.controller.client.helpers.domain.ServerIdentity;
import org.jboss.as.controller.client.helpers.domain.ServerStatus;
import org.wildfly.plugin.deployment.DeploymentExecutionException;
import org.wildfly.plugin.deployment.DeploymentFailureException;
import siebog.core.config.NodeConfig;

/**
 * A helper class for starting Siebog / JBoss nodes.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class NodeStarter {
	private static final Logger logger = Logger.getLogger(NodeStarter.class.getName());
	private NodeConfig config;

	// @formatter:off
	private final static String INTF_DEF = "" +
			"<interface name=\"management\"><inet-address value=\"${jboss.bind.address.management:ADDR}\" /></interface>" +
			"<interface name=\"public\"><inet-address value=\"${jboss.bind.address:ADDR}\" /></interface>" +
			"<interface name=\"unsecure\"><inet-address value=\"${jboss.bind.address.unsecure:ADDR}\" /></interface>";
	private final static String SLAVE_SERVER_DEF = "" +
			"<server name=\"NAME\" group=\"xjaf2x-group\" auto-start=\"true\">" +
			"  <socket-bindings port-offset=\"POFFSET\" />" +
			"</server>";
	// @formatter:on

	public NodeStarter(NodeConfig config) {
		this.config = config;
	}

	public void start() {
		try {
			if (config.isSlave())
				startSlave();
			else {
				startMaster();
				// TODO: check if already deployed
				deploy(config.getRootFolder(), Global.SIEBOG_MODULE);
			}
		} catch (DeploymentExecutionException | DeploymentFailureException | IOException ex) {
			throw new IllegalStateException("Error while starting node.", ex);
		}
	}

	private void startMaster() throws IOException {
		final String ADDR = config.getAddress();

		logger.info("Starting master node " + Global.MASTER_NAME + "@" + ADDR);
		String hostMaster = FileUtils
				.read(NodeStarter.class.getResourceAsStream("host-master.txt"));

		String intfDef = INTF_DEF.replace("ADDR", ADDR);
		hostMaster = hostMaster.replace("<!-- interface-def -->", intfDef);

		File hostConfig = new File(config.getJBossHome(), "domain/configuration/host-master.xml");
		FileUtils.write(hostConfig, hostMaster);

		// @formatter:off
		String[] jbossArgs = {
			"-jboss-home", config.getJBossHome().getAbsolutePath(),
			"-mp", new File(config.getJBossHome(), "modules").getAbsolutePath(),
			"-jar", new File(config.getJBossHome(), "jboss-modules.jar").getAbsolutePath(),
			"--",
			//"-Dorg.jboss.boot.log.file=file://" + jbossHome + "domain/log/xjaf.log",
			//"-Dlogging.configuration=file://" + jbossHome + "domain/configuration/logging.properties",
			"-server",
			"--",
			// 
			"--host-config=host-master.xml",
			"-Djboss.bind.address.management=" + ADDR
		};
		// @formatter:on

		org.jboss.as.process.Main.start(jbossArgs);
		waitForServer(ADDR, Global.MASTER_NAME, "master");
	}

	private void startSlave() throws IOException {
		final String ADDR = config.getAddress();
		final String MASTER = config.getMasterAddr();
		final String NAME = config.getSlaveName(); // + "@" + ADDR;
		final int portOffset = config.getPortOffset();

		logger.info(String.format("Starting slave node %s@%s, with %s@%s", NAME, ADDR,
				Global.MASTER_NAME, MASTER));
		String hostSlave = FileUtils.read(NodeStarter.class.getResourceAsStream("host-slave.txt"));

		String intfDef = INTF_DEF.replace("ADDR", ADDR);
		hostSlave = hostSlave.replace("<!-- interface-def -->", intfDef);

		String serverDef = SLAVE_SERVER_DEF.replace("NAME", ADDR).replace("POFFSET",
				portOffset + "");
		hostSlave = hostSlave.replace("<!-- server-def -->", serverDef);

		int nativePort = 9999;
		if (portOffset > 0)
			nativePort += portOffset;
		hostSlave = hostSlave.replace("NAT_PORT", nativePort + "");

		hostSlave = hostSlave.replace("SL_NAME", "name=\"" + NAME + "\"");

		File hostConfig = new File(config.getJBossHome(), "domain/configuration/host-slave.xml");
		FileUtils.write(hostConfig, hostSlave);

		// @formatter:off
		String[] jbossArgs = {
			"-jboss-home", config.getJBossHome().getAbsolutePath(),
			"-mp", new File(config.getJBossHome(), "modules").getAbsolutePath(),
			"-jar", new File(config.getJBossHome(), "jboss-modules.jar").getAbsolutePath(),
			"--",
			//"-Dorg.jboss.boot.log.file=" + jbossHome + "domain/log/host-controller.log",
			//"-Dlogging.configuration=file:" + jbossHome + "domain/configuration/logging.properties",
			"-server",
			"--",
			// 
			"--host-config=host-slave.xml",
			"-Djboss.domain.master.address=" + MASTER,
			"-Djboss.bind.address=" + ADDR,
			"-Djboss.bind.address.management=" + ADDR
		};
		// @formatter:on

		org.jboss.as.process.Main.start(jbossArgs);
	}

	private void waitForServer(String address, String serverName, String hostName) {
		try {
			InetAddress addr = InetAddress.getByName(address);
			ServerStatus status;
			int maxTries = 10;
			do {
				Thread.sleep(500);
				try (DomainClient client = DomainClient.Factory.create(addr, 9990)) {
					ServerIdentity id = new ServerIdentity(hostName, Global.GROUP, serverName);
					try {
						Map<ServerIdentity, ServerStatus> statuses = client.getServerStatuses();
						status = statuses.get(id);
					} catch (RuntimeException e) {
						final Throwable cause = e.getCause();
						if (cause != null && (cause instanceof IOException)) {
							if (--maxTries < 0)
								throw e;
							status = ServerStatus.STARTING;
						} else
							throw e;
					}
				}
			} while (status == ServerStatus.STARTING || status == ServerStatus.UNKNOWN);
		} catch (Throwable ex) {
			throw new IllegalStateException("Error while waiting for the server to start: "
					+ ex.getMessage());
		}
	}

	private void deploy(File root, String name) throws DeploymentExecutionException,
			DeploymentFailureException, IOException {
		final String appName = name + ".war";
		File file = new File(root, appName);
		logger.info("Deploying " + file.getCanonicalPath());
		InetAddress addr = InetAddress.getByName(config.getAddress());
		Deployment.deploy(addr, file, appName);
	}
}
