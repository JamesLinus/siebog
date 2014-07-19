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

package xjaf;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import xjaf.server.config.XjafCluster;
import xjaf.server.config.XjafCluster.Mode;

/**
 * A helper class for starting XJAF / JBoss nodes.
 *
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class StartNode
{
	private static final Logger logger = Logger.getLogger(StartNode.class.getName());
	private static String jbossHome;

	// @formatter:off
	private final static String INTF_DEF = "" +
			"<interface name=\"management\"><inet-address value=\"${jboss.bind.address.management:ADDR}\" /></interface>" +
			"<interface name=\"public\"><inet-address value=\"${jboss.bind.address:ADDR}\" /></interface>" +
			"<interface name=\"unsecure\"><inet-address value=\"${jboss.bind.address.unsecure:ADDR}\" /></interface>";
	private final static String SLAVE_SERVER_DEF = "" +
			"<server name=\"NAME\" group=\"xjaf-group\" auto-start=\"true\" />";
	// @formatter:on

	private static void runMaster() throws IOException
	{
		final String ADDR = XjafCluster.get().getAddress();

		logger.info("Starting master node xjaf-master@" + ADDR);
		String hostMaster = Global.readFile(StartNode.class.getResourceAsStream("host-master.txt"));

		String intfDef = INTF_DEF.replace("ADDR", ADDR);
		hostMaster = hostMaster.replace("<!-- interface-def -->", intfDef);

		File hostConfig = new File(jbossHome + "domain/configuration/host-master.xml");
		Global.writeFile(hostConfig, hostMaster);

		// @formatter:off
		String[] jbossArgs = {
			"-jboss-home", jbossHome,
			"-mp", jbossHome + "modules",
			"-jar", jbossHome + "jboss-modules.jar",
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
	}

	public static void runSlave() throws IOException
	{
		final String ADDR = XjafCluster.get().getAddress();
		final String MASTER = XjafCluster.get().getMaster();
		final String NAME = "xjaf@" + ADDR;

		logger.info(String.format("Starting slave node %s, with xjaf-master@%s", NAME, MASTER));
		String hostSlave = Global.readFile(StartNode.class.getResourceAsStream("host-slave.txt"));

		String intfDef = INTF_DEF.replace("ADDR", ADDR);
		hostSlave = hostSlave.replace("<!-- interface-def -->", intfDef);

		String serverDef = SLAVE_SERVER_DEF.replace("NAME", NAME);
		hostSlave = hostSlave.replace("<!-- server-def -->", serverDef);

		File hostConfig = new File(jbossHome + "domain/configuration/host-slave.xml");
		Global.writeFile(hostConfig, hostSlave);

		// @formatter:off
		String[] jbossArgs = {
			"-jboss-home", jbossHome,
			"-mp", jbossHome + "modules",
			"-jar", jbossHome + "jboss-modules.jar",
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

	private static void createConfigFile(String[] args, File configFile) throws IOException,
			SAXException, ParserConfigurationException
	{
		Mode mode = null;
		String address = null, master = null;
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
				try
				{
					mode = Mode.valueOf(value.toUpperCase());
				} catch (IllegalArgumentException ex)
				{
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
			case "--help":
			case "help":
			case "--h":
			case "h":
				throw new IllegalArgumentException();
			}
		}

		if (address == null)
			throw new IllegalArgumentException("Please specify the address of this node.");

		if (mode == Mode.MASTER)
		{
			if (master != null)
				throw new IllegalArgumentException("Master address should be specified "
						+ "on the master node only.");
		} else
		{
			if (hasSlaveNodes)
				throw new IllegalArgumentException("The list of slave nodes should "
						+ "be specified only on the master node.");
			if (master == null)
				throw new IllegalArgumentException("Please specify the master node's address.");
		}

		// ok, create the file
		writeConfigFile(configFile, mode, address, slaveNodes, master);
	}
	
	private static void writeConfigFile(File configFile, Mode mode, String address, Set<String> slaveNodes, String master) throws IOException
	{
		String str = Global.readFile(StartNode.class.getResourceAsStream("xjaf-config.txt"));
		str = str.replace("%mode%", mode.toString());
		str = str.replace("%address%", address);
		if (mode == Mode.MASTER)
		{
			StringBuilder slaves = new StringBuilder();
			if (slaveNodes != null)
			{
				String comma = "";
				for (String sl: slaveNodes)
				{
					slaves.append(comma).append(sl);
					if (comma.equals(""))
						comma = ",";
				}
			}
			str = str.replace("%slave_list%", slaves.toString());
			str = str.replace("%master_addr%", "");
		} else
		{
			String masterAddr = "master=\"" + master + "\"";
			str = str.replace("%master_addr%", masterAddr);
			str = str.replace("%slave_list%", "");
		}
		Global.writeFile(configFile, str);
	}

	private static void printUsage()
	{
		System.out.println("USAGE: " + StartNode.class.getSimpleName() + " [args]");
		System.out.println("args:");
		System.out.println("\t--mode:\t\tMASTER or SLAVE");
		System.out.println("\t--address:\t\tNetwork address of this computer.");
		System.out.println("\t--master:\t\tIf SLAVE, the master node's network address.");
		System.out.println("\t--slaves:\t\tIf MASTER, a comma-separated "
				+ "list of all at least one slave node.");
	}
	
	private static String getRootFolder()
	{
		String root = "";
		java.security.CodeSource codeSource = StartNode.class.getProtectionDomain()
				.getCodeSource();
		try
		{
			String path = codeSource.getLocation().toURI().getPath();
			File jarFile = new File(path);
			if (path.lastIndexOf(".jar") > 0)
				root = jarFile.getParentFile().getPath();
			else
				// get out of build/classes
				root = jarFile.getParentFile().getParentFile().getPath();
		} catch (Exception ex)
		{
		}
		root = root.replace('\\', '/');
		if (!root.endsWith("/"))
			root += "/";
		return root;
	}

	public static void main(String[] args)
	{
		Global.printVersion();
		
		String xjafRootStr = System.getProperty("xjaf.base.dir");
		if (xjafRootStr == null)
		{
			xjafRootStr = getRootFolder();
			logger.info("System property 'xjaf.base.dir' not defined, using " + xjafRootStr);
		}
		XjafCluster.setXjafRoot(new File(xjafRootStr));
		
		try
		{
			if (args.length > 0)
				createConfigFile(args, XjafCluster.getConfigFile());
			else if (!XjafCluster.getConfigFile().exists()) // use the default settings
				writeConfigFile(XjafCluster.getConfigFile(), Mode.MASTER, "localhost", null, null);
				
			XjafCluster.init(false);
			jbossHome = XjafCluster.getJBossHome();
			if (XjafCluster.get().getMode() == Mode.MASTER)
				runMaster();
			else
				runSlave();
		} catch (IllegalArgumentException ex)
		{
			System.out.println(ex.getMessage());
			printUsage();
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "", ex);
		}
	}
}
