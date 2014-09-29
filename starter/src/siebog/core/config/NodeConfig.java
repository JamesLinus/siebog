package siebog.core.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;
import siebog.core.FileUtils;

public class NodeConfig {
	private static final Logger logger = Logger.getLogger(NodeConfig.class.getName());
	private File jbossHome;
	private File rootFolder;
	private File configFile;
	private NodeInfo node;
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
				throw new IllegalArgumentException("Environment variable JBOSS_HOME not (properly) set.");
			rootFolder = new File(new File(jbossHome, "..").getCanonicalPath());
			// configuration file
			configFile = new File(getRootFolder(), "siebog.properties");

			if (args != null && args.length > 0)
				createConfigFromArgs(args);

			if (configFile.exists())
				logger.info("Loading configuration from " + configFile);
			else {
				logger.info("Creating default configuration file " + configFile.toString());
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
		try (BufferedReader in = new BufferedReader(new FileReader(configFile))) {
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim().toLowerCase();
				if (line.startsWith("node="))
					node = new NodeInfo(line.substring(5));
			}
		}
		if (node == null)
			throw new IllegalArgumentException("Parameter --node not specified.");
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
