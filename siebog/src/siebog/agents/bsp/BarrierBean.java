package siebog.agents.bsp;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import siebog.agentmanager.AID;
import siebog.agentmanager.Agent;
import siebog.agentmanager.AgentManagerBean;
import siebog.agentmanager.XjafAgent;
import siebog.messagemanager.ACLMessage;
import siebog.messagemanager.Performative;
import siebog.utils.ObjectFactory;

@Stateful
@Remote(Agent.class)
@LocalBean
@Lock(LockType.WRITE)
public class BarrierBean extends XjafAgent {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(BarrierBean.class);
	public static final String PROTOCOL = "siebog-bsp";
	private static final long TIMEOUT = 10000;
	private int superstep;
	private Set<AID> registered;
	// agents that are processing messages in the current superste
	private Set<AID> processing;
	@Inject
	private AgentManagerBean localAgm;

	public BarrierBean() {
		registered = new HashSet<>();
		processing = new HashSet<>();
	}

	@Override
	protected void onMessage(ACLMessage msg) {
		switch (msg.performative) {
		case SUBSCRIBE:
			registerAgent((AID) msg.contentObj);
			break;
		case CANCEL:
			deregisterAgent((AID) msg.contentObj);
			break;
		case INFORM:
			agentCompletedSuperstep((AID) msg.contentObj);
			break;
		case REQUEST: // timeout
			onTimeout((Superstep) msg.contentObj);
			break;
		default:
			break;
		}
	}

	private void registerAgent(AID aid) {
		registered.add(aid);
		nextSuperstepIfPossible();
		LOG.info("Registered agent {}.", aid);
	}

	private void deregisterAgent(AID aid) {
		registered.remove(aid);
		if (processing.remove(aid)) {
			nextSuperstepIfPossible();
		}
		LOG.info("Deregistered agent {}.", aid);
	}

	private void agentCompletedSuperstep(AID aid) {
		processing.remove(aid);
		nextSuperstepIfPossible();
	}

	private void onTimeout(Superstep superstep) {
		if (superstep.getCounter() == this.superstep) {
			LOG.info("Barrier timeout in superstep #{}, pending agents: {}",
					superstep.getCounter(), processing);
			filterUnavailableAgents();
			if (!nextSuperstepIfPossible()) {
				scheduleTimeout();
			}
		}
	}

	private boolean nextSuperstepIfPossible() {
		if (processing.isEmpty() && !registered.isEmpty()) {
			processing.addAll(registered);
			signalSuperstep(superstep + 1, processing);
			scheduleTimeout();
			return true;
		}
		return false;
	}

	private void signalSuperstep(int superstep, Set<AID> receivers) {
		this.superstep = superstep;
		ACLMessage msg = buildSuperstepMsg(receivers);
		ObjectFactory.getMessageManager().post(msg);
	}

	private ACLMessage buildSuperstepMsg(Set<AID> receivers) {
		ACLMessage msg = new ACLMessage(Performative.INFORM);
		msg.sender = myAid;
		msg.protocol = PROTOCOL;
		msg.receivers.addAll(processing);
		msg.contentObj = buildSuperstep();
		return msg;
	}

	private void scheduleTimeout() {
		ACLMessage msg = new ACLMessage(Performative.REQUEST);
		msg.receivers.add(myAid);
		msg.contentObj = buildSuperstep();
		msm().post(msg, TIMEOUT);
	}

	private void filterUnavailableAgents() {
		Iterator<AID> i = processing.iterator();
		while (i.hasNext()) {
			AID aid = i.next();
			if (agentAlive(aid)) {
				signalSuperstep(superstep, Collections.singleton(aid));
			} else {
				i.remove();
			}
		}
	}

	private boolean agentAlive(AID aid) {
		try {
			Agent agent = localAgm.getAgentReference(aid);
			agent.ping();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private Superstep buildSuperstep() {
		return new Superstep(myAid.getName(), superstep);
	}
}
