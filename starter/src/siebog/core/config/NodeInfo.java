package siebog.core.config;

public class NodeInfo {
	private boolean slave;
	private String address;
	private String name; // if slave
	private String masterAddr; // if slave

	/**
	 * 
	 * @param config If only 'address', then this is a master node. Otherwise, it should be
	 *            'name@address,master@master_address'.
	 */
	public NodeInfo(String config) {
		config = config.trim();
		if (config.isEmpty())
			throw new IllegalArgumentException();
		int to = config.indexOf("->");
		if (to == -1) {
			slave = false;
			address = config;
		} else { // slave@address->master
			int at = config.indexOf('@');
			name = config.substring(0, at).trim();
			address = config.substring(at + 1, to).trim();
			masterAddr = config.substring(to + 2).trim();
			if (name.isEmpty() || address.isEmpty() || masterAddr.isEmpty())
				throw new IllegalArgumentException(
						"A slave node needs a name, its own address, and the master's address.");
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
			return String.format("%s@%s,master@%s", name, address, masterAddr);
		return address;
	}
}
