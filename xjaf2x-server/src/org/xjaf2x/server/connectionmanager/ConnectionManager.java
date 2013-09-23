package org.xjaf2x.server.connectionmanager;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Startup;
import javax.ejb.Singleton;
import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;
import org.xjaf2x.server.config.RelayInfo;
import org.xjaf2x.server.config.ServerConfig;

/**
 * Default connection manager implementation.
 * 
 * @author <a href="mailto:mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
@Singleton
@Remote(ConnectionManagerI.class)
@Startup
public class ConnectionManager implements ConnectionManagerI
{
	private static final Logger logger = Logger.getLogger(ConnectionManager.class.getName());
	private JChannel channel;
	
	@PostConstruct
	public void postConstruct()
	{
		RelayInfo relay = ServerConfig.getRelay();
		if (relay == null)
		{
			if (logger.isLoggable(Level.INFO))
				logger.info("Relay not specified, support for remote clusters disabled");
		}
		else
		{
			try
			{
				channel = new JChannel(ServerConfig.class.getResource("site-config.xml"));
				channel.connect("xjaf2x-global-cluster");
				channel.setReceiver(new ReceiverAdapter() {
					
				});
				if (logger.isLoggable(Level.INFO))
					logger.info("ConnectionManager initialized");
			} catch (Exception ex)
			{
				logger.log(Level.SEVERE, "Unable to create channel", ex);
			}
		}
	}
}
