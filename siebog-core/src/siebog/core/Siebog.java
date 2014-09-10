package siebog.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import siebog.core.config.NodeConfig;

public class Siebog {
	private static final Logger logger = Logger.getLogger(Siebog.class.getName());

	private static void printUsage() {
		System.out.println("USAGE: " + NodeStarter.class.getSimpleName() + " args");
		System.out.println("args:");
		System.out.println("\t--mode:\t\tMASTER or SLAVE");
		System.out.println("\t--address:\t\tNetwork address of this computer.");
		System.out.println("\t--master:\t\tIf SLAVE, the master node's network address.");
		System.out.println("\t--name:\t\tIf SLAVE, the name of this slave node.");
		System.out.println("\t--port-offset:\t\tIf SLAVE, optional, socket port offset.");
		// TODO check if the list of slaves is really needed
		System.out.println("\t--slaves:\t\tIf MASTER, a comma-separated " + "list of all or at least one slave node.");
	}

	public static void main(String[] args) {
		Global.printVersion();
		try {
			NodeConfig config;
			if (args.length == 0)
				config = NodeConfig.get();
			else {
				logger.info("Building configuration from command-line arguments.");
				config = NodeConfig.get(args);
			}

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
