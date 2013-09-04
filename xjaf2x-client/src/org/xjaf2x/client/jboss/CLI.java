package org.xjaf2x.client.jboss;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandContextFactory;
import org.jboss.as.controller.client.helpers.domain.DomainClient;
import org.jboss.as.controller.client.helpers.domain.ServerIdentity;
import org.jboss.as.controller.client.helpers.domain.ServerStatus;
import org.xjaf2x.client.FileUtils;
import org.xjaf2x.server.Deployment;
import org.xjaf2x.server.Global;

public class CLI
{
	private static final Logger logger = Logger.getLogger(CLI.class.getName());
	private String jbossHome;
	private boolean isMaster;
	private String masterAddr;
	//private CommandContext cmdCtx;
	//private ModelControllerClient ctrlClient;
	private DomainClient domainClient;

	// @formatter:off
	private final static String INTF_DEF = "" +
			"<interface name=\"management\"><inet-address value=\"${jboss.bind.address.management:ADDR}\" /></interface>" +
			"<interface name=\"public\"><inet-address value=\"${jboss.bind.address:ADDR}\" /></interface>" +
			"<interface name=\"unsecure\"><inet-address value=\"${jboss.bind.address.unsecure:ADDR}\" /></interface>";
	private final static String SLAVE_SERVER_DEF = "" +
			"<server name=\"NAME\" group=\"" + Global.GROUP + "\" auto-start=\"true\" />";
	// @formatter:on

	public CLI() throws Exception
	{
		jbossHome = System.getenv("JBOSS_HOME");
		if ((jbossHome == null) || !new File(jbossHome).isDirectory())
			throw new Exception("Environment variable JBOSS_HOME not set");
		jbossHome = jbossHome.replace('\\', '/');
		if (!jbossHome.endsWith("/"))
			jbossHome += "/";

		// overwrite "domain.xml"
		String domain = FileUtils.read(getClass().getResourceAsStream("/res/domain.xml"));
		try (PrintWriter out = new PrintWriter(jbossHome + "domain/configuration/domain.xml"))
		{
			out.print(domain);
		} catch (Exception ex)
		{
			logger.log(Level.WARNING, "Error while overwriting 'domain.xml'", ex);
		}
	}

	public void runMaster(String address) throws Exception
	{
		String hostMaster = FileUtils.read(getClass().getResourceAsStream("/res/host-master.xml"));

		String intfDef = INTF_DEF.replace("ADDR", address);
		hostMaster = hostMaster.replace("<!-- interface-def -->", intfDef);

		final String hostConfig = jbossHome + "domain/configuration/host-master.xml";
		FileUtils.write(hostConfig, hostMaster);

		// @formatter:off
		String[] jbossArgs = {
			"-jboss-home", "\"" + jbossHome + "\"",
			"-mp", "\"" + jbossHome + "modules\"",
			"-jar", "\"" + jbossHome + "jboss-modules.jar\"",
			"--",
			"\"-Dorg.jboss.boot.log.file=" + jbossHome + "domain/log/xjaf2x.log\"",
			"\"-Dlogging.configuration=file:" + jbossHome + "domain/configuration/logging.properties\"",
			"-server",
			"--",
			// 
			"--host-config=host-master.xml",
			"\"-Djboss.bind.address.management=" + address + "\"",
		};
		// @formatter:on
		
		org.jboss.as.process.Main.start(jbossArgs);
		isMaster = true;
		masterAddr = address;
	}
	
	public void runSlave(String slaveAddr, String serverName, String masterAddr) throws Exception
	{
		String hostSlave = FileUtils.read(getClass().getResourceAsStream("/res/host-slave.xml"));

		String intfDef = INTF_DEF.replace("ADDR", slaveAddr);
		hostSlave = hostSlave.replace("<!-- interface-def -->", intfDef);

		String serverDef = SLAVE_SERVER_DEF.replace("NAME", serverName);
		hostSlave = hostSlave.replace("<!-- server-def -->", serverDef);

		final String hostConfig = jbossHome + "domain/configuration/host-slave.xml";
		FileUtils.write(hostConfig, hostSlave);

		// @formatter:off
		String[] jbossArgs = {
			"-jboss-home", "\"" + jbossHome + "\"",
			"-mp", "\"" + jbossHome + "modules\"",
			"-jar", "\"" + jbossHome + "jboss-modules.jar\"",
			"--",
			"\"-Dorg.jboss.boot.log.file=" + jbossHome + "domain/log/host-controller.log\"",
			"\"-Dlogging.configuration=file:" + jbossHome + "domain/configuration/logging.properties\"",
			"-server",
			"--",
			// 
			"--host-config=host-slave.xml",
			"\"-Djboss.domain.master.address=" + masterAddr + "\"",
			"\"-Djboss.bind.address=" + slaveAddr + "\"",
			"\"-Djboss.bind.address.management=" + slaveAddr + "\"",
		};
		// @formatter:on
		org.jboss.as.process.Main.start(jbossArgs);
		isMaster = false;
		this.masterAddr = masterAddr;
	}
	
	public void redeployServer() throws Exception
	{
		if (domainClient == null)
		{
			CommandContext ctx = CommandContextFactory.getInstance().newCommandContext(masterAddr, 9999, Global.USERNAME, Global.PASSWORD.toCharArray());
			ctx.connectController();

			domainClient = DomainClient.Factory.create(ctx.getModelControllerClient());
			if (!waitServerStart(Global.MASTER_NAME))
				throw new Exception("The server could not be started, please see the log file for more details");			
		}
		
		if (logger.isLoggable(Level.INFO))
			logger.info("Deploying XJAF 2.x server...");
		Deployment deployment = new Deployment(domainClient);
		deployment.deploy(Global.SERVER, new File(jbossHome + Global.SERVER + ".jar"));
	}	
	
	private boolean waitServerStart(String serverName)
	{
		if (logger.isLoggable(Level.INFO))
			logger.info("Waiting for the server to start...");
		while (true)
		{
			boolean found = false;
			for (Map.Entry<ServerIdentity, ServerStatus> e: domainClient.getServerStatuses().entrySet())
			{
				if (e.getKey().getServerName().equals(serverName))
				{
					found = true;
					switch (e.getValue())
					{
					case STARTED:
						return true;
					case STARTING:
						try
						{
							Thread.sleep(50);
						} catch (Exception ex)
						{
						}
						break;
					default:
						return false;
					}
				}
				if (!found)
				{
					if (logger.isLoggable(Level.INFO))
						logger.info("Server [" + serverName + "] not found, aborting...");
					return false;
				}
			}
		} 
	}

	public boolean isMaster()
	{
		return isMaster;
	}

	public String getJbossHome()
	{
		return jbossHome;
	}

	public String getMasterAddr()
	{
		return masterAddr;
	}
}
