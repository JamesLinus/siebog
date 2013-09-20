package org.xjaf2x.run;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Main
{
	private static String jbossHome;
	// @formatter:off
	private final static String INTF_DEF = "" +
			"<interface name=\"management\"><inet-address value=\"${jboss.bind.address.management:ADDR}\" /></interface>" +
			"<interface name=\"public\"><inet-address value=\"${jboss.bind.address:ADDR}\" /></interface>" +
			"<interface name=\"unsecure\"><inet-address value=\"${jboss.bind.address.unsecure:ADDR}\" /></interface>";
	private final static String SLAVE_SERVER_DEF = "" +
			"<server name=\"NAME\" group=\"xjaf2x-group\" auto-start=\"true\" />";
	// @formatter:on
		
	private static void printUsage()
	{
		System.out.println("Arguments: node address [name master]\n"
				+ "\tnode\t\t--master OR --slave\n"
				+ "\taddress\t\tNetwork address of this computer\n"
				+ "\tname\t\tIf slave, name of the node\n"
				+ "\tmaster\t\tIf slave, network address of the master node");
		System.exit(-1);
	}
	
	private static void runMaster(String address) throws IOException
	{
		String hostMaster = FileUtils.read(Main.class.getResourceAsStream("/res/host-master.xml"));

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
	}
	
	public static void runSlave(String slaveAddr, String serverName, String masterAddr) throws IOException 
	{
		String hostSlave = FileUtils.read(Main.class.getResourceAsStream("/res/host-slave.xml"));

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
	}
	
	public static void main(String[] args) throws IOException
	{
		jbossHome = System.getenv("JBOSS_HOME");
		if ((jbossHome == null) || !new File(jbossHome).isDirectory())
		{
			System.out.println("Environment variable JBOSS_HOME not set");
			System.exit(-1);
		}
		jbossHome = jbossHome.replace('\\', '/');
		if (!jbossHome.endsWith("/"))
			jbossHome += "/";
		
		// overwrite "domain.xml"
		String domain = FileUtils.read(Main.class.getResourceAsStream("/res/domain.xml"));
		try (PrintWriter out = new PrintWriter(jbossHome + "domain/configuration/domain.xml"))
		{
			out.print(domain);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
		
		if (args.length < 2)
			printUsage();
		
		boolean master = false;
		if (args[0].equalsIgnoreCase("--master"))
			master = true;
		else if (args[0].equalsIgnoreCase("--slave"))
			master = false;
		else
			printUsage();
		
		if (master)
			runMaster(args[1]);
		else if (args.length != 4)
			printUsage();
		else
			runSlave(args[1], args[2], args[3]);
	}
}
