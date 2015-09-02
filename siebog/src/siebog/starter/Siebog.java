package siebog.starter;

import static java.lang.System.out;
import java.util.logging.Level;
import java.util.logging.Logger;
import siebog.starter.Global;
import siebog.starter.NoJBossHomeException;
import siebog.starter.NodeStarter;
import siebog.starter.Siebog;
import siebog.starter.config.NodeConfig;

public class Siebog {
	private static final Logger logger = Logger.getLogger(Siebog.class.getName());

	public static void main(String[] args) {
		Global.printVersion();

		try {
			NodeConfig config = NodeConfig.get(args);
			NodeStarter starter = new NodeStarter(config);
			starter.start();
			if (!config.isSlave())
				logger.info("Siebog node ready.");
		} catch (IllegalArgumentException ex) {
			logger.severe(ex.getMessage());
			printUsage();
		} catch (NoJBossHomeException ex) {
			logger.log(Level.SEVERE, ex.getMessage());
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Failed to start the node, an unknown error occurred.", ex);
		}
	}

	private static void printUsage() {
		out.println("USAGE: java -jar siebog.war NodeDescription");
		out.println("NodeDescription describes this Siebog node. If the node is master, use --node=address.");
		out.println("Otherwise, use --node=name@address-master where name is the cluster-wide unique name of the slave node,");
		out.println("address is its network address, and master is the network address of a running master node.");
	}
}
