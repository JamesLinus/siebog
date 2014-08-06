package siebog.server.xjaf.dnarslayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class DNarsGraphFactory
{
	public static DNarsGraphI create(String domain) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Class<?> cls = Class.forName("siebog.server.dnars.graph.DNarsGraphFactory");
		Method m = cls.getMethod("create", String.class, Map.class);
		return (DNarsGraphI) m.invoke(null, domain, null);
	}
}
