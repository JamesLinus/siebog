package radigost.samples.jade.predators.predator;

import jade.core.Agent;

@SuppressWarnings("serial")
public class Predator extends Agent
{
	private Predation predation;
	
	@Override
	public void setup()
	{
		System.out.println(getAID().getName() + " ready");
		predation = new Predation(this);
		addBehaviour(predation);
	}
}
