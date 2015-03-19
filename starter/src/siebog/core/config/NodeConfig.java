package siebog.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import siebog.core.FileUtils;

public class NodeConfig {
	private static final Logger logger = Logger.getLogger(NodeConfig.class.getName());
	private File jbossHome;
	private File rootFolder;
	private File configFile;
	private NodeInfo node;
	private String cassandraHost;
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
			String homeStr = System.getenv("JBOSS_HOME");
			if (homeStr == null || homeStr.isEmpty())
				throw new IllegalStateException(
						"Environment variable JBOSS_HOME not (properly) set.");
			// get JBoss home folder
			jbossHome = new File(homeStr);
			// make sure it is set correctly
			File modules = new File(jbossHome, "jboss-modules.jar");
			if (!modules.exists())
				throw new IllegalStateException(
						"Environment variable JBOSS_HOME not (properly) set.");
			rootFolder = new File(new File(jbossHome, "..").getCanonicalPath());
			// configuration file
			configFile = new File(getRootFolder(), "siebog.properties");

			boolean hasArgs = args != null && args.length > 0;
			if (hasArgs)
				createConfigFromArgs(args);

			if (configFile.exists()) {
				if (hasArgs)
					logger.info("Loaded configuration from program arguments. Configuration has been stored in "
							+ configFile + " for future use.");
				else
					logger.info("Loading configuration from " + configFile);
			} else {
				logger.info("Creating default configuration file " + configFile);
				makeConfigFile(new NodeInfo("localhost"));
			}
			loadConfig();
		} catch (IOException ex) {
			throw new IllegalArgumentException("Input/output error: " + ex.getMessage(), ex);
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

	private void loadConfig() throws IOException {
		Properties p = new Properties();
		try (InputStream in = new FileInputStream(configFile)) {
			p.load(in);
			String nodeStr = p.getProperty("node");
			if (nodeStr == null || nodeStr.isEmpty())
				throw new IllegalArgumentException("Parameter --node not specified.");
			node = new NodeInfo(nodeStr);
			this.cassandraHost = p.getProperty("cassandra.host", "localhost");
		}
	}

	public boolean isSlave() {
		return node.isSlave();
	}

	public String getAddress() {
		return node.getAddress();
	}

	public String getMasterAddr() {
		return node.getMasterAddr();
	}

	public int getPortOffset() {
		return 0;
	}

	public RelayInfo getRelay() {
		return null;
	}

	public String getSlaveName() {
		return node.getName();
	}

	public String getCassandraHost() {
		return cassandraHost;
	}

	private void createConfigFromArgs(String[] args) throws IOException {
		NodeInfo node = null;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			int n = arg.indexOf('=');
			if (n <= 0 || n >= arg.length() - 1)
				throw new IllegalArgumentException("Invalid argument: " + arg);
			String name = arg.substring(0, n).toLowerCase();
			String value = arg.substring(n + 1);

			switch (name) {
			case "--node":
			case "-node":
			case "node":
				node = new NodeInfo(value);
				break;
			case "--help":
			case "-help":
			case "help":
			case "--h":
			case "-h":
			case "h":
			case "/?":
				throw new IllegalArgumentException();
			}
		}

		if (node == null)
			throw new IllegalArgumentException("Parameter --node not specified.");
		makeConfigFile(node);
	}

	private void makeConfigFile(NodeInfo fromNode) throws IOException {
		FileUtils.write(configFile, "node=" + fromNode);
	}
}
