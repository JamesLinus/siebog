package siebog.agents;

import java.io.Serializable;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.SiebogClient;
import siebog.core.Global;
import siebog.interaction.ACLMessage;
import siebog.interaction.bsp.BarrierFactory;
import siebog.interaction.bsp.OnSuperstep;
import siebog.interaction.bsp.Superstep;
import siebog.utils.ObjectFactory;

@Stateful
@Remote(Agent.class)
public class BSPAgent extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(BSPAgent.class);
	private static final String BARRIER_ID = "default";
	@Inject
	private BarrierFactory barrierFactory;

	@Override
	protected void onInit(AgentInitArgs args) {
		barrierFactory.getBarrier(BARRIER_ID).register(myAid);
	}

	@Override
	protected void onTerminate() {
		barrierFactory.getBarrier(BARRIER_ID).deregister(myAid);
	}

	@OnSuperstep(barrier = BARRIER_ID)
	public void onSuperstep(Superstep superstep, Serializable param) {
		LOG.info("Agent {} executing superstep #{}.", myAid, superstep.getCounter());
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		// process regular messages
	}

	public static void main(String[] args) {
		SiebogClient.connect("localhost");

		AgentClass cls = new AgentClass(Global.SIEBOG_MODULE, BSPAgent.class.getSimpleName());
		ObjectFactory.getAgentManager().startServerAgent(cls, "bsp" + System.currentTimeMillis(), null);
	}
}