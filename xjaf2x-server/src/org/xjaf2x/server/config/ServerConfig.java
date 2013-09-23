package org.xjaf2x.server.config;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Helper class for reading global server configuration.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class ServerConfig
{
	private static final Logger logger = Logger.getLogger(ServerConfig.class.getName());
	private static Document doc;
	private static NodeList agents;
	private static RelayInfo relay;

	static
	{
		try
		{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			try (InputStream is = ServerConfig.class.getResourceAsStream("xjaf2x-server.xml"))
			{
				doc = builder.parse(is);
			}
		} catch (Exception ex)
		{
			logger.log(Level.WARNING, "Error while reading server configuration", ex);
		}
	}

	public static NodeList getAgents()
	{
		if (doc == null)
			return null;
		if (agents == null)
			agents = doc.getElementsByTagName("agent");
		return agents;
	}

	public static RelayInfo getRelay()
	{
		if (doc == null)
			return null;
		if (relay == null)
		{
			NodeList list = doc.getElementsByTagName("relay");
			if ((list == null) || (list.getLength() != 1))
				return null;
			Element elem = (Element) list.item(0);
			String address = elem.getAttribute("address");
			String site = elem.getAttribute("site");
			relay = new RelayInfo(address, site);
		}
		return relay;
	}
}
