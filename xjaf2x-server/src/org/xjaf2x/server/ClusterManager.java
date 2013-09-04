package org.xjaf2x.server;

import java.util.List;
import java.util.Properties;
import javax.naming.NamingException;
import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;

public class ClusterManager
{
	public static void init(List<String> addresses) throws NamingException
	{
		Properties p = new Properties();
		p.put("endpoint.name", "client-endpoint");
		p.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
		p.put("remote.clusters", "ejb");
		//p.put("remote.cluster.ejb.clusternode.selector", "org.jboss.ejb.client.RandomClusterNodeSelector");
		p.put("remote.cluster.ejb.clusternode.selector", "org.xjaf2x.server.MyClusterNodeSelector");
		
		StringBuilder connections = new StringBuilder();
		String sep = "";
		for (String str : addresses)
		{
			String addr = "C_" + str.replace('.', '_');
			final String id = "remote.connection." + addr;
			
			p.put(id + ".host", str);
			p.put(id + ".port", "4447");
			p.put(id + ".connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
			p.put(id + ".username", Global.USERNAME);
			p.put(id + ".password", Global.PASSWORD);
			
			connections.append(sep).append(addr);
			if (sep.length() == 0)
				sep = ",";
		}
		
		p.put("remote.connections", connections.toString());
		
		EJBClientConfiguration cc = new PropertiesBasedEJBClientConfiguration(p);
		ContextSelector<EJBClientContext> selector = new ConfigBasedEJBClientContextSelector(cc);
		EJBClientContext.setSelector(selector);
	}
}
