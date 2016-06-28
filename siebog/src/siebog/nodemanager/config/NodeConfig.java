package siebog.nodemanager.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import siebog.nodemanager.NoJBossHomeException;
import siebog.nodemanager.config.NodeConfig;
import siebog.nodemanager.config.NodeInfo;
import siebog.nodemanager.config.RelayInfo;

public class NodeConfig {
	private static final Logger logger = Logger.getLogger(NodeConfig.class.getName());
	private File jbossHome;
	private File rootFolder;
	private File configFile;
	private NodeInfo node = new NodeInfo("localhost");
	private String cassandraHost;
	private String logLevel = "INFO";
	private static NodeConfig instance;

	public static synchronized NodeConfig get() {
		return get(null);
	}

	public static synchronized NodeConfig get(String[] args) {
		if (instance == null)
			instance = new NodeConfig(args);
		return instance;
	}

	private NodeConfig(String[] args) {
		this.jbossHome = detectJBossHome();
		try {
			rootFolder = new File(new File(jbossHome, "..").getCanonicalPath());
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
		if (args != null && args.length > 0) {
			parseArgs(args);
		} else {
			configFile = new File(getRootFolder(), "siebog.properties");
			if (!configFile.exists()) {
				throw new IllegalArgumentException("No configuration file nor program arguments.");
			}
			parseConfigFile();
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

	private File detectJBossHome() {
		String home = System.getenv("JBOSS_HOME");
		if (home == null || home.isEmpty())
			throw new NoJBossHomeException();
		// make sure it is set correctly
		File modules = new File(home, "jboss-modules.jar");
		if (!modules.exists())
			throw new NoJBossHomeException();
		return new File(home);
	}

	private void parseArgs(String[] args) {
		logger.info("Loading configuration from program arguments.");
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
			case "--cassandra.host":
			case "-cassandra.host":
			case "cassandra.host":
				cassandraHost = value;
				break;
			case "--loglevel":
			case "-loglevel":
			case "loglevel":
				logLevel = value.toUpperCase();
				if (!logLevel.equals("DEBUG") && !logLevel.equals("INFO")
						&& !logLevel.equals("WARN") && !logLevel.equals("ERROR")) {
					throw new IllegalArgumentException("loglevel should be one of the following: "
							+ "debug, info, warn, error");
				}
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
			throw new IllegalArgumentException("Parameter --node is required.");
	}

	private void parseConfigFile() {
		logger.info("Loading configuration from " + configFile.getAbsolutePath());
		Properties p = new Properties();
		try (InputStream in = new FileInputStream(configFile)) {
			p.load(in);
			String nodeStr = p.getProperty("node");
			if (nodeStr == null || nodeStr.isEmpty())
				throw new IllegalArgumentException("Parameter --node is required.");
			node = new NodeInfo(nodeStr);
			this.cassandraHost = p.getProperty("cassandra.host", "localhost");
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public String getLogLevel() {
		return logLevel;
	}
}
