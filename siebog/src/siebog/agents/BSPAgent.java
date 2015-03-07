package siebog.agents;

import java.io.Serializable;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.interaction.ACLMessage;
import siebog.interaction.bsp.BarrierService;
import siebog.interaction.bsp.OnSuperstep;
import siebog.interaction.bsp.Superstep;
import siebog.utils.ObjectFactory;
import siebog.xjaf.agentmanager.AgentInitArgs;
import siebog.xjaf.core.Agent;
import siebog.xjaf.core.XjafAgent;

@Stateful
@Remote(Agent.class)
public class BSPAgent extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(BSPAgent.class);
	// barrier used by this agent
	private static final String BARRIER_ID = "default";
	// a helper singleton service for accessing the barrier
	private static final BarrierService service = ObjectFactory.getBarrierService();

	@Override
	protected void onInit(AgentInitArgs args) {
		service.getBarrier(BARRIER_ID).register(myAid);
	}

	@Override
	protected void onTerminate() {
		service.getBarrier(BARRIER_ID).deregister(myAid);
	}

	@OnSuperstep
	public void onSuperstep(Superstep superstep, Serializable param) {
		LOG.info("Agent {} executing superstep #{}.", myAid, superstep.getCounter());
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		// process regular messages
	}
}
