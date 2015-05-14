package dnars.siebog.agents;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import siebog.SiebogClient;
import siebog.agents.Agent;
import siebog.agents.AgentBuilder;
import dnars.siebog.DNarsAgent;
import dnars.siebog.annotations.Beliefs;
import dnars.siebog.annotations.Domain;

@Stateful
@Remote(Agent.class)
@Domain(name = "Hello")
public class HelloDNars extends DNarsAgent {
	private static final long serialVersionUID = 1L;

	@Beliefs
	public String[] init() {
		return new String[] { "tiger -> animal (1.0, 0.9)" };
	}

	public static void main(String[] args) {
		SiebogClient.connect("localhost");
		AgentBuilder.module("dnars-web").ejb(HelloDNars.class).randomName().start();
	}
}
