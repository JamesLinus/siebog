package org.xjaf2x.server.config;

/**
 * Relay configuration, used to connect remote clusters.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class RelayInfo
{
	private final String address;
	private final String site;

	public RelayInfo(String address, String site)
	{
		this.address = address;
		this.site = site;
	}

	public String getAddress()
	{
		return address;
	}

	public String getSite()
	{
		return site;
	}
}
