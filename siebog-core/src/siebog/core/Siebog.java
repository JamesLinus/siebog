package siebog.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import siebog.core.config.NodeConfig;

public class Siebog {
	private static final Logger logger = Logger.getLogger(Siebog.class.getName());

	private static void printUsage() {
		System.out.println("USAGE: java -jar siebog-start.jar NodeDescription");
		System.out.println("NodeDescription");
		System.out.println("\tDescribes this Siebog node.");
		System.out.println("\tIf the node is master, use --node=address.");
		System.out.println("\tOtherwise, use --node=name@address,master@master_address where");
		System.out.println("\t\tname: cluster-wide unique name of the slave node");
		System.out.println("\t\taddress: nodes network address");
		System.out.println("\t\tmaster_address: network address of a running master node.");
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
			logger.info("Siebog node ready.");

		} catch (IllegalArgumentException ex) {
			logger.info(ex.getMessage());
			printUsage();
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Failed to start the node.", ex);
		}
	}
}
