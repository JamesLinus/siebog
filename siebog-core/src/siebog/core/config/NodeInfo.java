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
		int at = config.indexOf('@');
		if (at == -1) {
			slave = false;
			address = config;
		} else {
			slave = true;
			int comma = config.indexOf(',');
			int mat = config.lastIndexOf('@');
			if (comma < at || mat < comma)
				throw new IllegalArgumentException();
			name = config.substring(0, at).trim();
			address = config.substring(at + 1, comma).trim();
			masterAddr = config.substring(mat + 1).trim();
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
			return String.format("%s@%s,master@%s", name, address, masterAddr);
		return address;
	}
}
