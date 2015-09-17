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
import java.net.InetAddress;
import java.util.Map;
import java.util.logging.Logger;
import org.jboss.as.controller.client.helpers.domain.DomainClient;
import org.jboss.as.controller.client.helpers.domain.ServerIdentity;
import org.jboss.as.controller.client.helpers.domain.ServerStatus;
import org.wildfly.plugin.deployment.DeploymentExecutionException;
import org.wildfly.plugin.deployment.DeploymentFailureException;
import siebog.starter.Deployment;
import siebog.starter.FileUtils;
import siebog.starter.Global;
import siebog.starter.NodeStarter;
import siebog.starter.config.NodeConfig;

/**
 * A helper class for starting Siebog / JBoss nodes.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class NodeStarter {
	private static final Logger LOG = Logger.getLogger(NodeStarter.class.getName());
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
			setupDomain();
			setupLogging();
			if (config.isSlave()) {
				startSlave();
			} else {
				startMaster();
				// TODO: check if already deployed
				deploy(config.getRootFolder(), Global.SIEBOG_MODULE);
			}
		} catch (DeploymentExecutionException | DeploymentFailureException | IOException ex) {
			throw new IllegalStateException("Error while starting node.", ex);
		}
	}

	public void setupDomain() throws IOException {
		String resDomain = FileUtils.read(NodeStarter.class.getResourceAsStream("profile.xml"));
		resDomain = resDomain.replace("${loglevel}", "INFO");
		File domainFile = new File(config.getJBossHome(), "domain/configuration/domain.xml");
		String domain = FileUtils.read(domainFile);
		int start = domain.indexOf("<profile name=\"full-ha\">");
		int end = domain.indexOf("</profile>", start + 1) + "</profile>".length();
		StringBuilder str = new StringBuilder(domain);
		str.replace(start, end, resDomain);
		// removeDeployments(str);
		FileUtils.write(domainFile, str.toString());
	}

	@SuppressWarnings("unused")
	private void removeDeployments(StringBuilder str) {
		int a = str.lastIndexOf("<deployments>");
		if (a > 0) {
			String end = "</deployments>";
			int b = str.lastIndexOf(end);
			str.delete(a, b + end.length());
		}
		File dataDir = new File(config.getJBossHome(), "domain/data/content");
		if (dataDir.exists()) {
			dataDir.delete();
		}
	}

	private void setupLogging() throws IOException {
		String logProps = FileUtils.read(NodeStarter.class
				.getResourceAsStream("logging.properties"));
		logProps = logProps.replace("${loglevel}", "INFO");
		File logFile = new File(config.getJBossHome(), "domain/configuration/logging.properties");
		FileUtils.write(logFile, logProps);
	}

	private void startMaster() throws IOException {
		LOG.info("Starting master node " + Global.MASTER_NAME + "@" + config.getAddress());
		prepareHostMaster();
		// @formatter:off
		String[] jbossArgs = {
			"-jboss-home", config.getJBossHome().getAbsolutePath(),
			"-mp", new File(config.getJBossHome(), "modules").getAbsolutePath(),
			"-jar", new File(config.getJBossHome(), "jboss-modules.jar").getAbsolutePath(),
			"--",
			"-server",
			"--",
			"--host-config=host-master.xml",
			"-Djboss.bind.address.management=" + config.getAddress()
		};
		// @formatter:on
		org.jboss.as.process.Main.start(jbossArgs);
		waitForServer(config.getAddress(), Global.MASTER_NAME, "master");
	}

	private void prepareHostMaster() throws IOException {
		String intfDef = INTF_DEF.replace("ADDR", config.getAddress());
		String host = FileUtils.read("host-master.txt", 0);
		host = host.replace("<!-- interface-def -->", intfDef);
		File outFile = new File(config.getJBossHome(), "domain/configuration/host-master.xml");
		FileUtils.write(outFile, host);

	}

	private void startSlave() throws IOException {
		final String ADDR = config.getAddress();
		final String MASTER = config.getMasterAddr();
		final String NAME = config.getSlaveName(); // + "@" + ADDR;
		final int portOffset = config.getPortOffset();

		LOG.info(String.format("Starting slave node %s@%s, with %s@%s", NAME, ADDR,
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
		LOG.info("Deploying " + file.getCanonicalPath());
		InetAddress addr = InetAddress.getByName(config.getAddress());
		Deployment.deploy(addr, file, appName);
	}
}
