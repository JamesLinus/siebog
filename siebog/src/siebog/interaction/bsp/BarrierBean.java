package siebog.interaction.bsp;

import java.util.HashSet;
import java.util.Set;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import siebog.xjaf.core.AID;

@Stateful
@Remote(Barrier.class)
@LocalBean
public class BarrierBean implements Barrier {
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
		// TODO Auto-generated method stub

	}

	@Override
	public void deregister(AID aid) {
		// TODO Auto-generated method stub

	}

	@Remove
	@Override
	public void remove() {
	}
}
