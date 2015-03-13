package siebog.interaction.bsp;

import java.util.HashSet;
import java.util.Set;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.agents.AID;

@Stateful
@Remote(Barrier.class)
@LocalBean
public class BarrierBean implements Barrier {
	private static final Logger LOG = LoggerFactory.getLogger(BarrierBean.class);
	private Set<AID> registered;
	// agents that have registered themselves in the middle of a reasoning cycle.
	// they will be included in the list of all agents at the beginning of the next cycle
	private Set<AID> pending;

	public BarrierBean() {
		registered = new HashSet<>();
		pending = new HashSet<>();
	}

	@Override
	public void register(AID aid) {
		registered.add(aid);
		LOG.info("Registered agent {}.", aid);
	}

	@Override
	public void deregister(AID aid) {
		registered.remove(aid);
		LOG.info("Deregistered agent {}.", aid);
	}

	@Remove
	@Override
	public void remove() {
	}
}
