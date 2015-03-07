package siebog.interaction.bsp;

import siebog.xjaf.core.AID;

public interface Barrier {
	void register(AID aid);

	void deregister(AID aid);

	void remove();
}
