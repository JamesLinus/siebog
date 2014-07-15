package radigost.samples.jade.cnet;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class RunContractors
{
	public static void main(String[] args) throws Exception
	{
		if (args.length != 3)
		{
			System.out.println("args:\n\thost:port lowIndex highIndex");
			return;
		}
		
		int n = args[0].indexOf(':');
		String host = args[0].substring(0, n);
		String port = args[0].substring(n + 1);
		
		int low = Integer.parseInt(args[1]);
		int high = Integer.parseInt(args[2]);
		
		Properties p = new Properties();
		p.setProperty(Profile.MAIN, "false");
		p.setProperty(Profile.MAIN_HOST, host);
		p.setProperty(Profile.MAIN_PORT, port);
		AgentContainer ac = Runtime.instance().createAgentContainer(new ProfileImpl(p));
	
		for (int i = low; i < high; i++)
		{
			String nick = "Contractor" + i;
			AgentController ag = ac.acceptNewAgent(nick, new Contractor());
			ag.start();
		}
		
		System.out.println("Contractors ready");
	}
}
