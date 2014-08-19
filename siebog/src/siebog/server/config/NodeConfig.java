package siebog.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NodeConfig
{
	private static final Logger logger = Logger.getLogger(NodeConfig.class.getName());
	private boolean isMaster;
	private String address;
	private String master;
	private int portOffset; // if slave
	private Set<String> clusterNodes;
	private RelayInfo relay;
	private String slaveName;

	public NodeConfig() throws SAXException, IOException,
			ParserConfigurationException
	{
		logger.info("Loading configuration from " + getConfigFile().getAbsolutePath());
		validateConfig();
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		try (InputStream is = new FileInputStream(getConfigFile()))
		{
			Document doc = builder.parse(is);
			loadConfig(doc);
		}
	}
	
	public static File getJBossHome() throws IOException
	{
		// TODO : make sure it works if there are spaces in the path
		String jbossHome = System.getenv("JBOSS_HOME");
		if (jbossHome == null || jbossHome.length() == 0 || !new File(jbossHome).isDirectory())
			throw new IOException("Environment variable JBOSS_HOME not set.");
		jbossHome = jbossHome.replace('\\', '/');
		return new File(jbossHome);
	}
	
	public static File getConfigFile() throws IOException
	{
		return new File(getJBossHome(), "../xjaf-config.xml");
	}

	private void validateConfig() throws SAXException, IOException
	{
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(getClass().getResource("xjaf-server.xsd"));
		Validator validator = schema.newValidator();
		validator.validate(new StreamSource(getConfigFile()));
	}

	private void loadConfig(Document doc) throws SAXException
	{
		// server properties
		NodeList list = doc.getElementsByTagName("server");
		Element elem = (Element) list.item(0);

		// mode
		String str = elem.getAttribute("mode");
		switch (str.toLowerCase())
		{
		case "master":
			isMaster = true;
			break;
		case "slave":
			isMaster = false;
			break;
		default:
			throw new IllegalArgumentException("Invalid mode: " + str);
		}

		// my address
		address = elem.getAttribute("address");

		if (isMaster)
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
					for (String s : slaveList)
					{
						s = s.trim();
						if (s.length() > 0)
							clusterNodes.add(s);
					}
				}
			}
		} else
		{
			// master address
			master = elem.getAttribute("master");
			if (master == null)
				throw new IllegalArgumentException("Please specify the master node's address.");
			// slave name
			slaveName = elem.getAttribute("name");
			if (slaveName == null)
				throw new IllegalArgumentException("Please specify the name of this slave node.");
			try
			{
				portOffset = Integer.parseInt(elem.getAttribute("port-offset"));
			} catch (NumberFormatException ex)
			{
				portOffset = 0;
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

	public boolean isMaster()
	{
		return isMaster;
	}

	public String getAddress()
	{
		return address;
	}

	public String getMaster()
	{
		return master;
	}

	public int getPortOffset()
	{
		return portOffset;
	}

	public Set<String> getClusterNodes()
	{
		return clusterNodes;
	}

	public RelayInfo getRelay()
	{
		return relay;
	}

	public String getSlaveName()
	{
		return slaveName;
	}
}
