package radigost.server;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import radigost.server.websocket.bridges.BridgeManager;

public class Global
{
	private static final Logger logger = Logger.getLogger(Global.class.getName());
	private static Context context;
	private static BridgeManager bridgeManager;
	
	static
	{
		try
		{
			Hashtable<String, Object> jndiProps = new Hashtable<>();
			jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			context = new InitialContext(jndiProps);
		} catch (NamingException ex)
		{
			logger.log(Level.SEVERE, "Context initialization error.", ex);
		}
	}
	
	public static BridgeManager getBridgeManager() throws NamingException
	{
		if (bridgeManager == null)
		{
			String name = "ejb:/Radigost//" + BridgeManager.class;
			bridgeManager = (BridgeManager) context.lookup(name);
		}
		return bridgeManager;
	}
}
