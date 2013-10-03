package org.xjaf2x.server.config;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xjaf2x.server.Global;
import org.xml.sax.SAXException;

/**
 * Helper class for reading global server configuration.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class ServerConfig
{
	public static enum Mode
	{
		UNKNOWN, MASTER, SLAVE
	}

	private static final Logger logger = Logger.getLogger(ServerConfig.class.getName());
	private static Document doc;
	private static NodeList agents;
	private static RelayInfo relay;
	private static Mode mode = Mode.UNKNOWN;
	private static String address;
	private static String master;
	private static String name;
	private static List<String> clusterNodes;

	static
	{
		try
		{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			try (InputStream is = ServerConfig.class.getResourceAsStream("/xjaf2x-server.xml"))
			{
				doc = builder.parse(is);
				// TODO : validate against the schema
				loadConfig();
			}
		} catch (Exception ex)
		{
			logger.log(Level.WARNING, "Error while reading server configuration", ex);
		}
	}

	public static void initCluster() throws NamingException
	{
		if (clusterNodes == null)
		{
			logger.warning("ClusterManager.init() called, but there are no cluster nodes");
			return;
		}

		Properties p = new Properties();
		p.put("endpoint.name", "client-endpoint");
		p.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
		p.put("remote.clusters", "ejb");
		p.put("remote.cluster.ejb.username", Global.USERNAME);
		p.put("remote.cluster.ejb.password", Global.PASSWORD);
		// p.put("remote.cluster.ejb.clusternode.selector",
		// "org.jboss.ejb.client.RandomClusterNodeSelector");
		p.put("remote.cluster.ejb.clusternode.selector", "org.xjaf2x.server.MyClusterNodeSelector");

		StringBuilder connections = new StringBuilder();
		String sep = "";
		for (String str : clusterNodes)
		{
			String addr = "C_" + str.replace('.', '_');
			final String id = "remote.connection." + addr;

			p.put(id + ".host", str);
			p.put(id + ".port", "4447");
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
	}

	private static void loadConfig() throws SAXException
	{
		agents = doc.getElementsByTagName("agent");

		// relay
		NodeList list = doc.getElementsByTagName("relay");
		if ((list != null) && (list.getLength() == 1))
		{
			Element elem = (Element) list.item(0);
			String address = elem.getAttribute("address");
			String site = elem.getAttribute("site");
			relay = new RelayInfo(address, site);
		}

		// server properties
		list = doc.getElementsByTagName("server");
		if ((list == null) || (list.getLength() != 1))
			throw new SAXException("Invalid format of the configuration file: "
					+ "expected exactly 1 'server' node");
		Element elem = (Element) list.item(0);

		// mode
		String str = elem.getAttribute("mode");
		if (mode == null)
			throw new IllegalArgumentException("Configuration node 'server' requires "
					+ "an attribute 'mode'");
		try
		{
			mode = Mode.valueOf(str.toUpperCase());
		} catch (IllegalArgumentException ex)
		{
			throw new IllegalArgumentException("Invalid mode '" + str
					+ "', expected 'master', 'slave', or 'standalone'");
		}

		// my address
		address = elem.getAttribute("address");
		if ((address == null) || (address.length() == 0))
			throw new IllegalArgumentException(
					"Configuration node 'server' requires an attribute 'address'");

		// if slave, get my name and master address
		if (mode == Mode.SLAVE)
		{
			name = elem.getAttribute("name");
			master = elem.getAttribute("master");
			if ((name == null) || (master == null) || (name.length() == 0)
					|| (master.length() == 0))
				throw new IllegalArgumentException("Configuration node 'server' requires "
						+ "attributes 'name' and 'master'");
		} else if (mode == Mode.MASTER)
		{
			clusterNodes = new ArrayList<>();
			clusterNodes.add(address);
			// collect all slave nodes
			list = doc.getElementsByTagName("cluster");
			if ((list != null) && (list.getLength() > 0))
			{
				Node node = list.item(0).getFirstChild();
				while (node != null)
				{
					String addr = node.getNodeValue();
					if ((addr != null) && (addr.length() > 0))
						clusterNodes.add(addr);
					node = node.getNextSibling();
				}
			}
		}
	}

	public static NodeList getAgents()
	{
		return agents;
	}

	public static RelayInfo getRelay()
	{
		return relay;
	}

	public static Mode getMode()
	{
		return mode;
	}

	public static String getAddress()
	{
		return address;
	}

	public static String getMaster()
	{
		return master;
	}

	public static String getName()
	{
		return name;
	}

	@SuppressWarnings("unused")
	private static String getRootFolder()
	{
		String root = "";
		java.security.CodeSource codeSource = ServerConfig.class.getProtectionDomain()
				.getCodeSource();
		try
		{
			String path = codeSource.getLocation().toURI().getPath();
			File jarFile = new File(path);
			if (path.lastIndexOf(".jar") > 0)
				root = jarFile.getParentFile().getPath();
			else
				// get out of xjaf2x-server/build/classes
				root = jarFile.getParentFile().getParentFile().getParentFile().getPath();
		} catch (Exception ex)
		{
		}
		root = root.replace('\\', '/');
		if (!root.endsWith("/"))
			root += "/";
		return root;
	}
}
