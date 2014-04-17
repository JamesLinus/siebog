package xjaf2x.start;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import xjaf2x.server.config.ServerConfig;

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
		final String ADDR = ServerConfig.getAddress();

		if (logger.isLoggable(Level.INFO))
			logger.info("Starting master node xjaf2x-master@" + ADDR);
		String hostMaster = FileUtils.read(JBossCLI.class.getResourceAsStream("host-master.txt"));

		String intfDef = INTF_DEF.replace("ADDR", ADDR);
		hostMaster = hostMaster.replace("<!-- interface-def -->", intfDef);

		final String hostConfig = jbossHome + "domain/configuration/host-master.xml";
		FileUtils.write(hostConfig, hostMaster);

		// @formatter:off
		String[] jbossArgs = {
			"-jboss-home", jbossHome,
			"-mp", jbossHome + "modules",
			"-jar", jbossHome + "jboss-modules.jar",
			"--",
			"-Dorg.jboss.boot.log.file=file://" + jbossHome + "domain/log/xjaf2x.log",
			"-Dlogging.configuration=file://" + jbossHome + "domain/configuration/logging.properties",
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
		final String ADDR = ServerConfig.getAddress();
		final String NAME = ServerConfig.getName();
		final String MASTER = ServerConfig.getMaster();

		if (logger.isLoggable(Level.INFO))
			logger.info(String.format("Starting slave node %s@%s, with xjaf2x-master@%s", NAME,
					ADDR, MASTER));
		String hostSlave = FileUtils.read(JBossCLI.class.getResourceAsStream("host-slave.txt"));

		String intfDef = INTF_DEF.replace("ADDR", ADDR);
		hostSlave = hostSlave.replace("<!-- interface-def -->", intfDef);

		String serverDef = SLAVE_SERVER_DEF.replace("NAME", NAME);
		hostSlave = hostSlave.replace("<!-- server-def -->", serverDef);

		final String hostConfig = jbossHome + "domain/configuration/host-slave.xml";
		FileUtils.write(hostConfig, hostSlave);

		// @formatter:off
		String[] jbossArgs = {
			"-jboss-home", jbossHome,
			"-mp", jbossHome + "modules",
			"-jar", jbossHome + "jboss-modules.jar",
			"--",
			"-Dorg.jboss.boot.log.file=" + jbossHome + "domain/log/host-controller.log",
			"-Dlogging.configuration=file:" + jbossHome + "domain/configuration/logging.properties",
			"-server",
			"--",
			// 
			"--host-config=host-slave.xml",
			"-Djboss.domain.master.address=" + MASTER,
			"-Djboss.bind.address=" + ADDR,
			"-Djboss.bind.address.management=" + MASTER
		};
		// @formatter:on

		org.jboss.as.process.Main.start(jbossArgs);
	}

	public static void main(String[] args)
	{
		try
		{
			// TODO : make sure it works if there are spaces in the path
			jbossHome = System.getenv("JBOSS_HOME");
			if ((jbossHome == null) || (jbossHome.length() == 0)
					|| !new File(jbossHome).isDirectory())
				throw new IOException("Environment variable JBOSS_HOME not set.");
			jbossHome = jbossHome.replace('\\', '/');
			if (!jbossHome.endsWith("/"))
				jbossHome += "/";
			
			switch (ServerConfig.getMode())
			{
			case MASTER:
				runMaster();
				break;
			case SLAVE:
				runSlave();
				break;
			default:
				throw new IllegalArgumentException("Unsupported mode: " + ServerConfig.getMode());
			}
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "", ex);
			System.exit(-1);
		}
	}
}
