package siebog.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import siebog.core.config.NodeConfig;
import static java.lang.System.out;

public class Siebog {
	private static final Logger logger = Logger.getLogger(Siebog.class.getName());

	private static void printUsage() {
		out.println("USAGE: java -jar siebog-start.jar NodeDescription");
		out.println("NodeDescription describes this Siebog node. If the node is master, use --node=address.");
		out.println("Otherwise, use --node=name@address->master where name is the cluster-wide unique name of the slave node,");
		out.println("address is its network address, and master is the network address of a running master node.");
	}

	public static void main(String[] args) {
		Global.printVersion();
		try {
			NodeConfig config;
			if (args.length == 0)
				config = NodeConfig.get();
			else
				config = NodeConfig.get(args);

			NodeStarter starter = new NodeStarter(config);
			starter.start();
			if (!config.isSlave())
				logger.info("Siebog node ready.");

		} catch (IllegalArgumentException ex) {
			logger.severe(ex.getMessage());
			printUsage();
		} catch (IllegalStateException ex) {
			logger.severe(ex.getMessage());
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Failed to start the node.", ex);
		}
	}
}
