package siebog.interaction.bsp;

import siebog.agents.AID;

public interface Barrier {
	void register(AID aid);

	void deregister(AID aid);

	void remove();
}
