package siebog.core.config;

public class NodeInfo {
	private boolean slave;
	private String address;
	private String name; // if slave
	private String masterAddr; // if slave

	/**
	 * 
	 * @param config If only 'address', then this is a master node. Otherwise, it should be
	 *            'name@address-master_address'.
	 */
	public NodeInfo(String config) {
		config = config.trim();
		if (config.isEmpty())
			throw new IllegalArgumentException();
		int dash = config.indexOf("-");
		if (dash == -1) {
			slave = false;
			address = config;
		} else { // slave@address-master
			slave = true;
			final String msg = "A slave node needs a name, its own address, and the master's address.";
			int at = config.indexOf('@');
			if (at <= 0 || at >= dash - 1)
				throw new IllegalArgumentException(msg);
			name = config.substring(0, at).trim();
			address = config.substring(at + 1, dash).trim();
			masterAddr = config.substring(dash + 1).trim();
			if (name.isEmpty() || address.isEmpty() || masterAddr.isEmpty())
				throw new IllegalArgumentException();
		}
	}

	public boolean isSlave() {
		return slave;
	}

	public String getAddress() {
		return address;
	}

	public String getName() {
		return name;
	}

	public String getMasterAddr() {
		return masterAddr;
	}

	@Override
	public String toString() {
		if (slave)
			return String.format("%s@%s-%s", name, address, masterAddr);
		return address;
	}
}
