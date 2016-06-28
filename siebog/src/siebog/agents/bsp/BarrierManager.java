package siebog.agents.bsp;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.inject.Inject;

import siebog.agentmanager.AID;
import siebog.agentmanager.AgentBuilder;
import siebog.agentmanager.AgentManagerBean;
import siebog.messagemanager.ACLMsgBuilder;
import siebog.messagemanager.Performative;

@Singleton
@LocalBean
public class BarrierManager {
	@Inject
	private AgentManagerBean agm;

	public void register(String barrierName, AID aid) {
		AID barrierAid = getBarrierAid(barrierName);
		// @formatter:off
		ACLMsgBuilder
			.performative(Performative.SUBSCRIBE)
			.receivers(barrierAid)
			.contentObj(aid)
			.post();
		// @formatter:on
	}

	public void deregister(String barrierName, AID aid) {
		AID barrierAid = getBarrierAid(barrierName);
		// @formatter:off
		ACLMsgBuilder
			.performative(Performative.CANCEL)
			.receivers(barrierAid)
			.contentObj(aid)
			.post();
		// @formatter:on
	}

	public void agentCompletedSuperstep(Superstep superstep, AID aid) {
		AID barrierAid = getBarrierAid(superstep.getBarrierName());
		// @formatter:off
		ACLMsgBuilder
			.performative(Performative.INFORM)
			.receivers(barrierAid)
			.contentObj(aid)
			.post();
		// @formatter:on

	}

	private AID getBarrierAid(String barrierName) {
		AID aid = agm.getAIDByRuntimeName(barrierName);
		if (aid != null) {
			return aid;
		}
		return AgentBuilder.siebog().ejb(BarrierBean.class).name(barrierName).start();
	}
}
