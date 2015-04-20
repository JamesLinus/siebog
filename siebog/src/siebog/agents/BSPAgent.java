package siebog.agents;

import java.io.Serializable;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.SiebogClient;
import siebog.interaction.ACLMessage;
import siebog.interaction.bsp.BarrierManager;
import siebog.interaction.bsp.OnSuperstep;
import siebog.interaction.bsp.Superstep;

@Stateful
@Remote(Agent.class)
public class BSPAgent extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(BSPAgent.class);
	private static final String BARRIER_ID = "default";
	@Inject
	private BarrierManager barrierManager;

	@Override
	protected void onInit(AgentInitArgs args) {
		barrierManager.register(BARRIER_ID, myAid);
	}

	@Override
	protected void onTerminate() {
		barrierManager.deregister(BARRIER_ID, myAid);
	}

	@OnSuperstep(barrier = BARRIER_ID)
	public void onSuperstep(Superstep superstep, Serializable param) {
		LOG.info("Agent {} executing superstep #{}.", myAid.getName(), superstep.getCounter());
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		Superstep superstep = ((Superstep) msg.contentObj);
		LOG.info("Agent {} executing superstep #{}.", myAid.getName(), superstep.getCounter());
		barrierManager.agentCompletedSuperstep(superstep, myAid);
	}

	public static void main(String[] args) {
		SiebogClient.connect("192.168.213.1", "192.168.213.129");
		AgentBuilder.siebog().ejb(BSPAgent.class).startNInstances(2);
	}
}