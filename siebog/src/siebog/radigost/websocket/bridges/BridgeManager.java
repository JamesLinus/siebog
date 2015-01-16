package siebog.radigost.websocket.bridges;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.websocket.Session;
//import siebog.radigost.websocket.bridges.jade.JadeBridge;

@Singleton
@LocalBean
@Lock(LockType.WRITE)
public class BridgeManager
{
	private Map<String, AbstractBridge> bridges; // name -> implementation

	@PostConstruct
	public void postConstruct()
	{
		bridges = new HashMap<>();
	}
	
	@PreDestroy
	public void preDestroy()
	{
		for (AbstractBridge bridge : bridges.values())
			bridge.destroy();
		bridges.clear();
	}
	
	public void runBridge(String name, String host) throws BridgeException
	{
		if (bridges.containsKey(name))
			throw new IllegalArgumentException("Bridge " + name + " already exists.");
//		switch (name)
//		{
//		case JadeBridge.NAME:
//			bridges.put(name, new JadeBridge(host));
//			break;
//		}
	}
	
	public void stopBridge(String name)
	{
		AbstractBridge bridge = bridges.get(name);
		if (bridge != null)
		{
			bridges.remove(name);
			bridge.destroy();
		}
	}
	
	public void onRadigostCommand(Session client, String name, String value)
	{
		for (AbstractBridge b: bridges.values())
			b.onRadigostCommand(client, name, value);
	}
	
	public void onMessageFromRadigost(String message)
	{
		for (AbstractBridge b: bridges.values())
			b.onMessageFromRadigost(message);	
	}
}
