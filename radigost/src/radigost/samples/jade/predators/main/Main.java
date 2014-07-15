package radigost.samples.jade.predators.main;

import jade.core.Agent;

@SuppressWarnings("serial")
public class Main extends Agent
{
	@Override
	public void setup()
	{
		System.out.println(getAID().getName() + " ready");
		addBehaviour(new MainBehavior(this));
	}
}
