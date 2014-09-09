package siebog.core.config;

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
import siebog.core.Global;
import siebog.core.NodeStarter;

public class NodeConfig {
	private static final Logger logger = Logger.getLogger(NodeConfig.class.getName());
	private File jbossHome;
	private File rootFolder;
	private File configFile;
	private boolean isMaster;
	private String address;
	private String master;
	private int portOffset; // if slave
	private Set<String> clusterNodes;
	private RelayInfo relay;
	private String slaveName;
	private static NodeConfig instance;

	public static synchronized NodeConfig get(String[] args) {
		if (instance == null)
			instance = new NodeConfig(args);
		return instance;
	}

	public static synchronized NodeConfig get() {
		return get(null);

	}

	private NodeConfig(String[] args) {
		try {
			// get JBoss home folder
			jbossHome = new File(System.getenv("JBOSS_HOME"));
			// make sure it is set correctly
			File modules = new File(jbossHome, "jboss-modules.jar");
			if (!modules.exists())
				throw new IOException("Environment variable JBOSS_HOME not set.");
			rootFolder = new File(new File(jbossHome, "..").getCanonicalPath());
			// configuration file
			configFile = new File(getRootFolder(), "xjaf-config.xml");

			if (args != null && args.length > 0)
				createFromArgs(args);

			if (configFile.exists())
				logger.info("Loading configuration from " + configFile);
			else {
				logger.info("Creating default configuration file " + configFile.toString());
				makeConfigFile(true, "localhost", null, "", "", -1);
			}
			validateConfig();
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			try (InputStream is = new FileInputStream(configFile)) {
				Document doc = builder.parse(is);
				loadConfig(doc);
			}
		} catch (SAXException | IOException | ParserConfigurationException ex) {
			throw new IllegalArgumentException("Error while initializing node configuration.", ex);
		}
	}

	public File getJBossHome() {
		return jbossHome;
	}

	public File getRootFolder() {
		return rootFolder;
	}

	public File getConfigFile() {
		return configFile;
	}

	private void validateConfig() throws SAXException, IOException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(getClass().getResource("xjaf-server.xsd"));
		Validator validator = schema.newValidator();
		validator.validate(new StreamSource(configFile));
	}

	private void loadConfig(Document doc) throws SAXException {
		// server properties
		NodeList list = doc.getElementsByTagName("server");
		Element elem = (Element) list.item(0);

		// mode
		String str = elem.getAttribute("mode");
		switch (str.toLowerCase()) {
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

		if (isMaster) {
			clusterNodes = new HashSet<>();
			clusterNodes.add(address);
			// collect all slave nodes
			NodeList cluster = doc.getElementsByTagName("cluster");
			if (cluster != null) {
				String slaves = ((Element) cluster.item(0)).getAttribute("slaves");
				if (slaves != null) {
					String[] slaveList = slaves.split(",");
					for (String s : slaveList) {
						s = s.trim();
						if (s.length() > 0)
							clusterNodes.add(s);
					}
				}
			}
		} else {
			// master address
			master = elem.getAttribute("master");
			if (master == null)
				throw new IllegalArgumentException("Please specify the master node's address.");
			// slave name
			slaveName = elem.getAttribute("name");
			if (slaveName == null)
				throw new IllegalArgumentException("Please specify the name of this slave node.");
			try {
				portOffset = Integer.parseInt(elem.getAttribute("port-offset"));
			} catch (NumberFormatException ex) {
				portOffset = 0;
			}
		}

		// relay
		list = doc.getElementsByTagName("relay");
		if (list != null && list.getLength() == 1) {
			elem = (Element) list.item(0);
			String address = elem.getAttribute("address");
			String site = elem.getAttribute("site");
			relay = new RelayInfo(address, site);
		}
	}

	public boolean isMaster() {
		return isMaster;
	}

	public String getAddress() {
		return address;
	}

	public String getMaster() {
		return master;
	}

	public int getPortOffset() {
		return portOffset;
	}

	public Set<String> getClusterNodes() {
		return clusterNodes;
	}

	public RelayInfo getRelay() {
		return relay;
	}

	public String getSlaveName() {
		return slaveName;
	}

	public void makeConfigFile(boolean isMaster, String address, Set<String> slaveNodes, String master,
			String slaveName, int portOffset) throws IOException {
		String str = Global.readFile(NodeStarter.class.getResourceAsStream("xjaf-config.txt"));
		str = str.replace("%mode%", isMaster ? "master" : "slave");
		str = str.replace("%address%", address);
		if (isMaster) {
			StringBuilder slaves = new StringBuilder();
			if (slaveNodes != null) {
				String comma = "";
				for (String sl : slaveNodes) {
					slaves.append(comma).append(sl);
					if (comma.equals(""))
						comma = ",";
				}
			}
			str = str.replace("%slave_list%", slaves.toString());
			str = str.replace("%master_addr%", "");
			str = str.replace("%port_offset%", "");
			str = str.replace("%slave_name%", "");
		} else {
			String masterAddr = "master=\"" + master + "\"";
			str = str.replace("%master_addr%", masterAddr);
			str = str.replace("%slave_list%", "");
			str = str.replace("%slave_name%", "name=\"" + slaveName + "\"");
			if (portOffset >= 0)
				str = str.replace("%port_offset%", "port-offset=\"" + portOffset + "\"");
			else
				str = str.replace("%port_offset%", "");
		}
		Global.writeFile(configFile, str);
	}

	public void createFromArgs(String[] args) throws IOException, SAXException, ParserConfigurationException {
		boolean isMaster = false;
		String address = null, master = null, slaveName = null;
		int portOffset = -1;
		Set<String> slaveNodes = new HashSet<>();
		boolean hasSlaveNodes = false;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			int n = arg.indexOf('=');
			if (n <= 0 || n >= arg.length() - 1)
				throw new IllegalArgumentException("Invalid argument: " + arg);
			String name = arg.substring(0, n).toLowerCase();
			String value = arg.substring(n + 1);

			switch (name) {
			case "--mode":
				switch (value.toLowerCase()) {
				case "master":
					isMaster = true;
					break;
				case "slave":
					isMaster = false;
					break;
				default:
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
			case "--port-offset":
				portOffset = Integer.parseInt(value);
				if (portOffset < 0 || portOffset > 65535)
					throw new IllegalArgumentException("Port offset should be in the range of [0..65535].");
				break;
			case "--name":
				slaveName = value;
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

		if (isMaster) {
			if (master != null)
				throw new IllegalArgumentException("Master address should be specified " + "on the master node only.");
			if (portOffset >= 0)
				throw new IllegalArgumentException("Port offset should be specified " + "on the slave node only.");
			if (slaveName != null)
				throw new IllegalArgumentException("Slave name should not be specified " + "on the master node.");
		} else {
			if (hasSlaveNodes)
				throw new IllegalArgumentException("The list of slave nodes should "
						+ "be specified only on the master node.");
			if (master == null)
				throw new IllegalArgumentException("Please specify the master node's address.");
			if (slaveName == null)
				throw new IllegalArgumentException("Please specify the name of this slave node.");
		}

		// ok, create the file
		makeConfigFile(isMaster, address, slaveNodes, master, slaveName, portOffset);
	}
}
