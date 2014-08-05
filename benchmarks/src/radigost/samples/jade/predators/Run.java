package radigost.samples.jade.predators;

import jade.Boot;

public class Run
{
	public static void main(String[] args)
	{
		String[] jadeArgs = {
			"-agents",
			"main:radigost.samples.jade.predators.main.Main;" +
			"pred0:radigost.samples.jade.predators.predator.Predator;" +
			"pred1:radigost.samples.jade.predators.predator.Predator;" +
			"pred2:ragigost.samples.jade.predators.predator.Predator;" +
			"pred3:radigost.samples.jade.predators.predator.Predator"
		};
		Boot.main(jadeArgs);
	}

}
