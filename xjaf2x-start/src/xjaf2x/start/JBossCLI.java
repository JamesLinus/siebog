package xjaf2x.start;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import xjaf2x.server.config.Xjaf2xCluster;
import xjaf2x.server.config.Xjaf2xCluster.Mode;

public class JBossCLI
{
	private static final Logger logger = Logger.getLogger(JBossCLI.class.getName());
	private static String jbossHome;

	// @formatter:off
	private final static String INTF_DEF = "" +
			"<interface name=\"management\"><inet-address value=\"${jboss.bind.address.management:ADDR}\" /></interface>" +
			"<interface name=\"public\"><inet-address value=\"${jboss.bind.address:ADDR}\" /></interface>" +
			"<interface name=\"unsecure\"><inet-address value=\"${jboss.bind.address.unsecure:ADDR}\" /></interface>";
	private final static String SLAVE_SERVER_DEF = "" +
			"<server name=\"NAME\" group=\"xjaf2x-group\" auto-start=\"true\" />";
	// @formatter:on

	private static void runMaster() throws IOException
	{
		final String ADDR = Xjaf2xCluster.get().getAddress();

		logger.info("Starting master node xjaf2x-master@" + ADDR);
		String hostMaster = FileUtils.read(JBossCLI.class.getResourceAsStream("host-master.txt"));

		String intfDef = INTF_DEF.replace("ADDR", ADDR);
		hostMaster = hostMaster.replace("<!-- interface-def -->", intfDef);

		File hostConfig = new File(jbossHome + "domain/configuration/host-master.xml");
		FileUtils.write(hostConfig, hostMaster);

		// @formatter:off
		String[] jbossArgs = {
			"-jboss-home", jbossHome,
			"-mp", jbossHome + "modules",
			"-jar", jbossHome + "jboss-modules.jar",
			"--",
			//"-Dorg.jboss.boot.log.file=file://" + jbossHome + "domain/log/xjaf2x.log",
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
		final String ADDR = Xjaf2xCluster.get().getAddress();
		final String MASTER = Xjaf2xCluster.get().getMaster();
		final String NAME = "xjaf2x@" + ADDR;

		logger.info(String.format("Starting slave node %s, with xjaf2x-master@%s", NAME, MASTER));
		String hostSlave = FileUtils.read(JBossCLI.class.getResourceAsStream("host-slave.txt"));

		String intfDef = INTF_DEF.replace("ADDR", ADDR);
		hostSlave = hostSlave.replace("<!-- interface-def -->", intfDef);

		String serverDef = SLAVE_SERVER_DEF.replace("NAME", NAME);
		hostSlave = hostSlave.replace("<!-- server-def -->", serverDef);

		File hostConfig = new File(jbossHome + "domain/configuration/host-slave.xml");
		FileUtils.write(hostConfig, hostSlave);

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
		logger.info("Loading configuration from the program arguments.");
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
		String str = FileUtils.read(JBossCLI.class.getResourceAsStream("xjaf2x-server.txt"));
		str = str.replace("%mode%", mode.toString());
		str = str.replace("%address%", address);
		if (mode == Mode.MASTER)
		{
			StringBuilder slaves = new StringBuilder();
			for (String sl : slaveNodes)
				slaves.append("<slave address=\"").append(sl).append("\" />");
			str = str.replace("%slave_list%", slaves.toString());
			str = str.replace("%master_addr%", "");
		} else
		{
			String masterAddr = "master=\"" + master + "\"";
			str = str.replace("%master_addr%", masterAddr);
			str = str.replace("%slave_list%", "");
		}
		FileUtils.write(configFile, str);
	}

	private static void printUsage()
	{
		System.out.println("USAGE: java -jar xjaf2x-start.jar [args]");
		System.out.println("args:");
		System.out.println("\t--mode:\t\tMASTER or SLAVE");
		System.out.println("\t--address:\t\tNetwork address of this computer.");
		System.out.println("\t--master:\t\tIf SLAVE, the master node's network address.");
		System.out.println("\t--slaves:\t\tIf MASTER, a comma-separated "
				+ "list of all at least one slave node.");
	}

	public static void main(String[] args)
	{
		try
		{
			if (args.length > 0)
				createConfigFile(args, Xjaf2xCluster.getConfigFile());
			Xjaf2xCluster.init(false);
			jbossHome = Xjaf2xCluster.getJBossHome();
			if (Xjaf2xCluster.get().getMode() == Mode.MASTER)
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
