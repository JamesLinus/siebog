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

package xjaf2x.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import xjaf2x.server.Global;
import xjaf2x.server.MyClusterNodeSelector;

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
	private static RelayInfo relay;
	private static Mode mode = Mode.UNKNOWN;
	private static String address;
	private static String master;
	private static String name;
	private static List<String> clusterNodes;

	static
	{
		InputStream is = null;
		try
		{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			try
			{
				is = new FileInputStream(getXjaf2xRoot() + "xjaf2x-server.xml");
				logger.info("Using custom configuration file xjaf2x-server.xml");
			} catch (IOException ex)
			{
				is = ServerConfig.class.getResourceAsStream("/xjaf2x-server.xml");
			}
			
			doc = builder.parse(is);
			// TODO : validate against the schema
			loadConfig();
		} catch (Exception ex)
		{
			logger.log(Level.WARNING, "Error while reading server configuration", ex);
		} finally
		{
			if (is != null)
				try
				{
					is.close();
				} catch (IOException e)
				{
				}
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
		p.put("remote.cluster.ejb.clusternode.selector", MyClusterNodeSelector.class.getName());

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
		
		logger.info("Initialized cluster " + clusterNodes);
	}

	private static void loadConfig() throws SAXException
	{
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
			NodeList slaves = doc.getElementsByTagName("slave");
			if (slaves == null || slaves.getLength() == 0)
				clusterNodes.add(address);
			else
				for (int i = 0; i < slaves.getLength(); i++)
					clusterNodes.add(((Element)slaves.item(i)).getAttribute("address"));
		}
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
	
	public static String getJBossHome() throws IOException
	{
		// TODO : make sure it works if there are spaces in the path
		String jbossHome = System.getenv("JBOSS_HOME");
		if ((jbossHome == null) || (jbossHome.length() == 0)
				|| !new File(jbossHome).isDirectory())
			throw new IOException("Environment variable JBOSS_HOME not set.");
		jbossHome = jbossHome.replace('\\', '/');
		if (!jbossHome.endsWith("/"))
			jbossHome += "/";
		return jbossHome;
	}
	
	public static String getXjaf2xRoot() throws IOException
	{
		return getJBossHome() + "xjaf2x/";
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
