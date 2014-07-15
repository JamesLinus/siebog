package radigost.samples.jade.cnet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class RunManager
{
	public static void main(String[] args) throws Exception
	{
		if (args.length != 1)
		{
			System.out.println("args:\n\thost:port");
			return;
		}
		
		int n = args[0].indexOf(':');
		String host = args[0].substring(0, n);
		String port = args[0].substring(n + 1);
		
		Properties p = new Properties();
		p.setProperty(Profile.MAIN, "true");
		p.setProperty(Profile.LOCAL_HOST, host);
		p.setProperty(Profile.LOCAL_PORT, port);
		AgentContainer ac = Runtime.instance().createMainContainer(new ProfileImpl(p));
		
		Manager m = new Manager();
		AgentController ag = ac.acceptNewAgent("manager", m);
		ag.start();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Num contractors? ");
		int numC = Integer.parseInt(in.readLine());
		System.out.print("Msg length? ");
		int msgLen = Integer.parseInt(in.readLine());
		String agents = "0-" + numC + "@" + args[0] + "/JADE";
		m.go(agents, msgLen);
	}
}
