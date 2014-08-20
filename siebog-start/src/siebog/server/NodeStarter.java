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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.jboss.as.controller.client.helpers.domain.DomainClient;
import org.jboss.as.controller.client.helpers.domain.ServerIdentity;
import org.jboss.as.controller.client.helpers.domain.ServerStatus;
import org.wildfly.plugin.deployment.DeploymentExecutionException;
import org.wildfly.plugin.deployment.DeploymentFailureException;
import org.xml.sax.SAXException;
import siebog.server.config.NodeConfig;

/**
 * A helper class for starting XJAF / JBoss nodes.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class NodeStarter
{
	private static final Logger logger = Logger.getLogger(NodeStarter.class.getName());
	private static File jbossHome;
	private static NodeConfig config;

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

	private static void runMaster() throws IOException
	{
		final String ADDR = config.getAddress();

		logger.info("Starting master node " + Global.MASTER_NAME + "@" + ADDR);
		String hostMaster = Global.readFile(NodeStarter.class
				.getResourceAsStream("host-master.txt"));

		String intfDef = INTF_DEF.replace("ADDR", ADDR);
		hostMaster = hostMaster.replace("<!-- interface-def -->", intfDef);

		File hostConfig = new File(jbossHome, "domain/configuration/host-master.xml");
		Global.writeFile(hostConfig, hostMaster);

		// @formatter:off
		String[] jbossArgs = {
			"-jboss-home", jbossHome.getAbsolutePath(),
			"-mp", new File(jbossHome, "modules").getAbsolutePath(),
			"-jar", new File(jbossHome, "jboss-modules.jar").getAbsolutePath(),
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

	public static void runSlave() throws IOException
	{
		final String ADDR = config.getAddress();
		final String MASTER = config.getMaster();
		final String NAME = config.getSlaveName() + "@" + ADDR;
		final int portOffset = config.getPortOffset();

		logger.info(String.format("Starting slave node %s, with %s@%s", NAME, Global.MASTER_NAME,
				MASTER));
		String hostSlave = Global.readFile(NodeStarter.class.getResourceAsStream("host-slave.txt"));

		String intfDef = INTF_DEF.replace("ADDR", ADDR);
		hostSlave = hostSlave.replace("<!-- interface-def -->", intfDef);

		String serverDef = SLAVE_SERVER_DEF.replace("NAME", NAME).replace("POFFSET",
				portOffset + "");
		hostSlave = hostSlave.replace("<!-- server-def -->", serverDef);

		int nativePort = 9999;
		if (portOffset > 0)
			nativePort += portOffset;
		hostSlave = hostSlave.replace("NAT_PORT", nativePort + "");

		hostSlave = hostSlave.replace("SL_NAME", "name=\"" + NAME + "\"");

		File hostConfig = new File(jbossHome, "domain/configuration/host-slave.xml");
		Global.writeFile(hostConfig, hostSlave);

		// @formatter:off
		String[] jbossArgs = {
			"-jboss-home", jbossHome.getAbsolutePath(),
			"-mp", new File(jbossHome, "modules").getAbsolutePath(),
			"-jar", new File(jbossHome, "jboss-modules.jar").getAbsolutePath(),
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

	private static void waitForServer(String address, String serverName, String hostName)
	{
		try
		{
			InetAddress addr = InetAddress.getByName(address);
			ServerStatus status;
			int maxTries = 10;
			do
			{
				Thread.sleep(500);
				DomainClient client = DomainClient.Factory.create(addr, 9990);
				ServerIdentity id = new ServerIdentity(hostName, Global.GROUP, serverName);
				try
				{
					Map<ServerIdentity, ServerStatus> statuses = client.getServerStatuses();
					status = statuses.get(id);
				} catch (RuntimeException e)
				{
					final Throwable cause = e.getCause();
					if (cause != null && (cause instanceof IOException))
					{
						if (--maxTries < 0)
							throw e;
						status = ServerStatus.STARTING;
					} else
						throw e;
				}
			} while (status == ServerStatus.STARTING || status == ServerStatus.UNKNOWN);
		} catch (Throwable ex)
		{
			throw new IllegalStateException("Error while waiting for the server to start: "
					+ ex.getMessage());
		}
	}

	private static void createConfigFromArgs(String[] args, File configFile) throws IOException,
			SAXException, ParserConfigurationException
	{
		boolean isMaster = false;
		String address = null, master = null, slaveName = null;
		int portOffset = -1;
		Set<String> slaveNodes = new HashSet<>();
		boolean hasSlaveNodes = false;
		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			int n = arg.indexOf('=');
			if (n <= 0 || n >= arg.length() - 1)
				throw new IllegalArgumentException("Invalid argument: " + arg);
			String name = arg.substring(0, n).toLowerCase();
			String value = arg.substring(n + 1);

			switch (name)
			{
			case "--mode":
				switch (value.toLowerCase())
				{
				case "master":
					isMaster = true;
					break;
				case "slave":
					isMaster = false;
					break;
				default:
					throw new IllegalArgumentException("Unsupported mode: " + value);
				}
				break;
			case "--address":
				address = value;
				break;
			case "--master":
				master = value;
				break;
			case "--slaves":
				hasSlaveNodes = true;
				String[] cc = value.split(",");
				for (String s : cc)
					slaveNodes.add(s);
				break;
			case "--port-offset":
				portOffset = Integer.parseInt(value);
				if (portOffset < 0 || portOffset > 65535)
					throw new IllegalArgumentException(
							"Port offset should be in the range of [0..65535].");
				break;
			case "--name":
				slaveName = value;
				break;
			case "--help":
			case "help":
			case "--h":
			case "h":
				throw new IllegalArgumentException();
			}
		}

		if (address == null)
			throw new IllegalArgumentException("Please specify the address of this node.");

		if (isMaster)
		{
			if (master != null)
				throw new IllegalArgumentException("Master address should be specified "
						+ "on the master node only.");
			if (portOffset >= 0)
				throw new IllegalArgumentException("Port offset should be specified "
						+ "on the slave node only.");
			if (slaveName != null)
				throw new IllegalArgumentException("Slave name should not be specified "
						+ "on the master node.");
		} else
		{
			if (hasSlaveNodes)
				throw new IllegalArgumentException("The list of slave nodes should "
						+ "be specified only on the master node.");
			if (master == null)
				throw new IllegalArgumentException("Please specify the master node's address.");
			if (slaveName == null)
				throw new IllegalArgumentException("Please specify the name of this slave node.");
		}

		// ok, create the file
		writeConfigFile(configFile, isMaster, address, slaveNodes, master, slaveName, portOffset);
	}

	private static void writeConfigFile(File configFile, boolean isMaster, String address,
			Set<String> slaveNodes, String master, String slaveName, int portOffset)
			throws IOException
	{
		String str = Global.readFile(NodeStarter.class.getResourceAsStream("xjaf-config.txt"));
		str = str.replace("%mode%", isMaster ? "master" : "slave");
		str = str.replace("%address%", address);
		if (isMaster)
		{
			StringBuilder slaves = new StringBuilder();
			if (slaveNodes != null)
			{
				String comma = "";
				for (String sl : slaveNodes)
				{
					slaves.append(comma).append(sl);
					if (comma.equals(""))
						comma = ",";
				}
			}
			str = str.replace("%slave_list%", slaves.toString());
			str = str.replace("%master_addr%", "");
			str = str.replace("%port_offset%", "");
			str = str.replace("%slave_name%", "");
		} else
		{
			String masterAddr = "master=\"" + master + "\"";
			str = str.replace("%master_addr%", masterAddr);
			str = str.replace("%slave_list%", "");
			str = str.replace("%slave_name%", "name=\"" + slaveName + "\"");
			if (portOffset >= 0)
				str = str.replace("%port_offset%", "port-offset=\"" + portOffset + "\"");
			else
				str = str.replace("%port_offset%", "");
		}
		Global.writeFile(configFile, str);
	}

	private static void printUsage()
	{
		System.out.println("USAGE: " + NodeStarter.class.getSimpleName() + " [args]");
		System.out.println("args:");
		System.out.println("\t--mode:\t\tMASTER or SLAVE");
		System.out.println("\t--address:\t\tNetwork address of this computer.");
		System.out.println("\t--master:\t\tIf SLAVE, the master node's network address.");
		System.out.println("\t--name:\t\tIf SLAVE, the name of this slave node.");
		System.out.println("\t--port-offset:\t\tIf SLAVE, optional, socket port offset.");
		System.out.println("\t--slaves:\t\tIf MASTER, a comma-separated "
				+ "list of all or at least one slave node.");
	}

	private static File getRootFolder() throws IOException
	{
		/*String root = "";
		java.security.CodeSource codeSource = NodeStarter.class.getProtectionDomain()
				.getCodeSource();
		try
		{
			String path = codeSource.getLocation().toURI().getPath();
			File jarFile = new File(path);
			if (path.lastIndexOf(".jar") > 0)
				root = jarFile.getParentFile().getPath();
			else
				// get out of sibog-starter/bin
				root = jarFile.getParentFile().getParentFile().getPath();
		} catch (Exception ex)
		{
		}
		root = root.replace('\\', '/');
		return new File(root);*/
		return new File(NodeConfig.getJBossHome(), "..");
	}

	private static void doDeploy(File root, String name) throws UnknownHostException,
			DeploymentExecutionException, DeploymentFailureException
	{
		final String appName = name + ".war";
		File file = new File(root, appName);
		logger.info("Deploying " + file.getAbsolutePath());
		InetAddress addr = InetAddress.getByName(config.getAddress());
		Deployment.deploy(addr, file, appName);
	}

	public static void main(String[] args)
	{
		Global.printVersion();
		try
		{
			final File root = getRootFolder();
			final File configFile = NodeConfig.getConfigFile();
			if (args.length > 0)
			{
				logger.info("Building configuration from command-line arguments.");
				createConfigFromArgs(args, configFile);
			} else if (!configFile.exists()) // use the default settings
			{
				logger.info("Using default configuration (master node @ localhost).");
				writeConfigFile(configFile, true, "localhost", null, null, "", -1);
			}
			config = new NodeConfig();

			jbossHome = NodeConfig.getJBossHome();
			if (config.isMaster())
			{
				runMaster();
				// TODO: check if already deployed
				doDeploy(root, Global.SERVER);
				doDeploy(root, "siebog-agents");
				logger.info("Siebog node ready.");
			} else
				runSlave();
		} catch (IllegalArgumentException ex)
		{
			logger.info(ex.getMessage());
			printUsage();
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "", ex);
		}
	}
}
