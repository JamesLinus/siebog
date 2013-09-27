package org.xjaf2x.run;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class RunJBoss
{
	private static final Logger logger = Logger.getLogger(RunJBoss.class.getName());
	private static String jbossHome;
	private static String mode;
	private static String address;
	private static String name;
	private static String master;

	// @formatter:off
	private final static String INTF_DEF = "" +
			"<interface name=\"management\"><inet-address value=\"${jboss.bind.address.management:ADDR}\" /></interface>" +
			"<interface name=\"public\"><inet-address value=\"${jboss.bind.address:ADDR}\" /></interface>" +
			"<interface name=\"unsecure\"><inet-address value=\"${jboss.bind.address.unsecure:ADDR}\" /></interface>";
	private final static String SLAVE_SERVER_DEF = "" +
			"<server name=\"NAME\" group=\"xjaf2x-group\" auto-start=\"true\" />";
	// @formatter:on

	private static String getRootFolder()
	{
		String root = "";
		java.security.CodeSource codeSource = RunJBoss.class.getProtectionDomain().getCodeSource();
		try
		{
			String path = codeSource.getLocation().toURI().getPath();
			File jarFile = new File(path);
			if (path.lastIndexOf(".jar") > 0)
				root = jarFile.getParentFile().getPath();
			else
				// get out of run-jboss/bin
				root = jarFile.getParentFile().getParentFile().getPath();
		} catch (Exception ex)
		{
		}
		root = root.replace('\\', '/');
		if (!root.endsWith("/"))
			root += "/";
		return root;
	}

	private static void runMaster() throws IOException
	{
		if (logger.isLoggable(Level.INFO))
			logger.info("Starting master node xjaf2x-master@" + address);
		String hostMaster = FileUtils.read(RunJBoss.class.getResourceAsStream("/res/host-master.xml"));

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

	public static void runSlave() throws IOException
	{
		if (logger.isLoggable(Level.INFO))
			logger.info(String.format("Starting slave node %s@%s, with master @%s", name, address,
					master));
		String hostSlave = FileUtils.read(RunJBoss.class.getResourceAsStream("/res/host-slave.xml"));

		String intfDef = INTF_DEF.replace("ADDR", address);
		hostSlave = hostSlave.replace("<!-- interface-def -->", intfDef);

		String serverDef = SLAVE_SERVER_DEF.replace("NAME", name);
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
			"\"-Djboss.domain.master.address=" + master + "\"",
			"\"-Djboss.bind.address=" + address + "\"",
			"\"-Djboss.bind.address.management=" + address + "\"",
		};
		// @formatter:on
		org.jboss.as.process.Main.start(jbossArgs);
	}

	private static void loadConfig() throws IOException, ParserConfigurationException, SAXException
	{
		// load configuration
		File cfgFile = new File(getRootFolder() + "xjaf2x-server.xml");
		if (!cfgFile.exists())
			throw new IOException("Configuration file 'xjaf2x-server.xml' not found");
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(cfgFile);
		NodeList list = doc.getElementsByTagName("server");
		if ((list == null) || (list.getLength() != 1))
			throw new SAXException(
					"Invalid format of the configuration file: expected exactly 1 'server' node");

		Element elem = (Element) list.item(0);
		// mode
		mode = elem.getAttribute("mode");
		if ((mode == null) || (!mode.equalsIgnoreCase("master") && !mode.equalsIgnoreCase("slave")))
			throw new IOException(
					"Configuration node 'server' requires an attribute 'mode' of value 'master' or 'slave'");
		mode = mode.toLowerCase();
		// my address
		address = elem.getAttribute("address");
		if ((address == null) || (address.length() == 0))
			throw new IOException("Configuration node 'server' requires an attribute 'address'");
		// if slave, my name and master address
		if (mode.equals("slave"))
		{
			name = elem.getAttribute("name");
			master = elem.getAttribute("master");
			if ((name == null) || (master == null) || (name.length() == 0)
					|| (master.length() == 0))
				throw new IOException(
						"Configuration node 'server' requires attributes 'name' and 'master'");
		}
	}

	public static void main(String[] args)
	{
		try
		{
			jbossHome = System.getenv("JBOSS_HOME");
			if ((jbossHome == null) || (jbossHome.length() == 0)
					|| !new File(jbossHome).isDirectory())
				throw new IOException("Environment variable JBOSS_HOME not set");
			jbossHome = jbossHome.replace('\\', '/');
			if (!jbossHome.endsWith("/"))
				jbossHome += "/";

			loadConfig();

			// overwrite "domain.xml"
			String domain = FileUtils.read(RunJBoss.class.getResourceAsStream("/res/domain.xml"));
			try (PrintWriter out = new PrintWriter(jbossHome + "domain/configuration/domain.xml"))
			{
				out.print(domain);
			}

			if (mode.equals("master"))
				runMaster();
			else
				runSlave();
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "", ex);
			System.exit(-1);
		}
	}
}
