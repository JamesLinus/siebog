package dnars.siebog.agents;

import java.util.List;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.SiebogClient;
import siebog.agents.Agent;
import siebog.agents.AgentBuilder;
import siebog.agents.AgentInitArgs;
import siebog.interaction.ACLMessage;
import dnars.base.Statement;
import dnars.siebog.DNarsAgent;
import dnars.siebog.annotations.BeliefAdded;
import dnars.siebog.annotations.BeliefUpdated;
import dnars.siebog.annotations.Beliefs;
import dnars.siebog.annotations.Domain;

@Stateful
@Remote(Agent.class)
// knowledge domain for this agent is Hello
@Domain(name = "Hello")
public class HelloDNars extends DNarsAgent {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(HelloDNars.class);

	@Override
	protected void doInit(AgentInitArgs args) {
	}

	@Beliefs
	public String[] initialBeliefs() { // set of initial beliefs
		return new String[] { "tiger -> animal (1.0, 0.9)" };
	}

	@BeliefAdded
	public void anyBeliefAdded(List<Statement> beliefs) {
		LOG.info("anyBeliefAdded: {}", beliefs);
	}

	@BeliefAdded(subj = "tiger", copula = "->", pred = "animal")
	public void tigerIsTypeOfAnimal(List<Statement> beliefs) {
		LOG.info("tiger is a type of animal: {}", beliefs);
		// this will change the belief's truth-value, which in turn will invoke the next method
		graph().include(beliefs.toArray(new Statement[0]));
	}

	@BeliefUpdated
	public void anyBeliefUpdated(List<Statement> beliefs) {
		LOG.info("anyBeliefUpdated: {}", beliefs);
	}

	@Override
	protected void onAclMessage(ACLMessage msg) {
	}

	public static void main(String[] args) {
		SiebogClient.connect("localhost");
		AgentBuilder.module("dnars-web").ejb(HelloDNars.class).randomName().start();
	}
}
