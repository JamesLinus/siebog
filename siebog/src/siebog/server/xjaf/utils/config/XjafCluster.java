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

package siebog.server.xjaf.utils.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import siebog.server.xjaf.Global;

/**
 * Helper class for reading global server configuration.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class XjafCluster
{
	public static enum Mode
	{
		MASTER, SLAVE
	}
	private static final Logger logger = Logger.getLogger(XjafCluster.class.getName());
	private static XjafCluster instance;
	private Mode mode;
	private String address;
	private String master;
	private Set<String> clusterNodes;
	private RelayInfo relay;
	private static File xjafRoot;
	private static boolean initialized;
	
	public static File getXjaf2xRoot()
	{
		return xjafRoot;
	}

	public static void setXjafRoot(File xjafRoot)
	{
		XjafCluster.xjafRoot = xjafRoot;
	}
	
	public static void init(boolean initClientContext) throws IOException, ParserConfigurationException, SAXException, NamingException
	{
		if (instance == null)
		{
			instance = new XjafCluster();
			if (initClientContext && !initialized)
			{
				instance.initClientContext();
				initialized = true;
			}
		}
	}
	
	private XjafCluster() throws IOException, ParserConfigurationException, SAXException
	{
		File configFile = getConfigFile();
		validateConfig(configFile);
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		try (InputStream is = new FileInputStream(configFile))
		{
			Document doc = builder.parse(is);
			loadConfig(doc);
		}
	}
	
	private void validateConfig(File configFile) throws SAXException, IOException
	{
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory
				.newSchema(XjafCluster.class.getResource("xjaf-server.xsd"));
		Validator validator = schema.newValidator();
		validator.validate(new StreamSource(configFile));
	}

	private void initClientContext() throws NamingException
	{
		if (clusterNodes == null || clusterNodes.size() == 0)
		{
			logger.warning("Trying to initialize without specifying cluster nodes.");
			return;
		}

		Properties p = new Properties();
		p.put("endpoint.name", "client-endpoint");
		p.put("deployment.node.selector", RRDeploymentNodeSelector.class.getName());

		p.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
		p.put("remote.clusters", "ejb");
		p.put("remote.cluster.ejb.username", Global.USERNAME);
		p.put("remote.cluster.ejb.password", Global.PASSWORD);
		p.put("remote.cluster.ejb.clusternode.selector", RRClusterNodeSelector.class.getName());

		StringBuilder connections = new StringBuilder();
		String sep = "";
		for (String str : clusterNodes)
		{
			String addr = "C_" + str.replace('.', '_');
			final String id = "remote.connection." + addr;

			p.put(id + ".host", str);
			p.put(id + ".port", "8080");
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

		logger.info("Initialized EJB client context with cluster nodes " + clusterNodes);
	}

	private void loadConfig(Document doc) throws SAXException
	{
		// server properties
		NodeList list = doc.getElementsByTagName("server");
		Element elem = (Element) list.item(0);

		// mode
		String str = elem.getAttribute("mode");
		try
		{
			mode = Mode.valueOf(str.toUpperCase());
		} catch (IllegalArgumentException ex)
		{
			throw new IllegalArgumentException("Invalid mode: " + str);
		}

		// my address
		address = elem.getAttribute("address");

		// if slave, get master address
		if (mode == Mode.SLAVE)
		{
			// master address
			master = elem.getAttribute("master");
			if (master == null)
				throw new IllegalArgumentException("Please specify the master node's address.");
		} else
		{
			clusterNodes = new HashSet<>();
			clusterNodes.add(address);
			// collect all slave nodes
			NodeList cluster = doc.getElementsByTagName("cluster");
			if (cluster != null)
			{
				String slaves = ((Element) cluster.item(0)).getAttribute("slaves");
				if (slaves != null)
				{
					String[] slaveList = slaves.split(",");
					for (String s: slaveList)
					{
						s = s.trim();
						if (s.length() > 0)
							clusterNodes.add(s);
					}
				}
			}
		}

		// relay
		list = doc.getElementsByTagName("relay");
		if (list != null && list.getLength() == 1)
		{
			elem = (Element) list.item(0);
			String address = elem.getAttribute("address");
			String site = elem.getAttribute("site");
			relay = new RelayInfo(address, site);
		}
	}

	public RelayInfo getRelay()
	{
		return relay;
	}

	public Mode getMode()
	{
		return mode;
	}

	public String getAddress()
	{
		return address;
	}

	public String getMaster()
	{
		return master;
	}

	public static String getJBossHome() throws IOException
	{
		// TODO : make sure it works if there are spaces in the path
		String jbossHome = System.getenv("JBOSS_HOME");
		if (jbossHome == null || jbossHome.length() == 0 || !new File(jbossHome).isDirectory())
			throw new IOException("Environment variable JBOSS_HOME not set.");
		jbossHome = jbossHome.replace('\\', '/');
		if (!jbossHome.endsWith("/"))
			jbossHome += "/";
		return jbossHome;
	}

	public static XjafCluster get()
	{
		return instance;
	}
	
	public static File getConfigFile() throws IOException
	{
		return new File(xjafRoot, "xjaf-config.xml");
	}
}
