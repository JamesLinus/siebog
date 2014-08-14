package siebog.server.xjaf.dnarslayer;

import siebog.server.xjaf.agents.base.AID;

public interface DNarsGraphI
{
	void addObserver(AID aid);
	void addStatement(String statement);
}
