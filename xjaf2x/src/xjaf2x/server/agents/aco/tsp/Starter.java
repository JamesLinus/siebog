package xjaf2x.server.agents.aco.tsp;

import java.io.Serializable;
import java.util.logging.Logger;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import xjaf2x.Global;
import xjaf2x.server.agentmanager.AID;
import xjaf2x.server.agentmanager.Agent;
import xjaf2x.server.agentmanager.AgentI;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;

/**
 * Starter agent, entry point.
 * 
 * @author <a href="mailto:tntvteod@neobee.net">Teodor Najdan Trifunov</a>
 * @author <a href="mailto:milan.laketic@yahoo.com">Milan Laketic</a>
 */
@Stateless
@Remote(AgentI.class)
public class Starter extends Agent
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Starter.class.getName());

	@Override
	protected void onInit(Serializable... args)
	{
		logger.fine("Starter agent running.");

		AID mapAid = new AID(Global.SERVER, Global.getEjbName(Map.class), "Map");
		agm.start(mapAid, args[1]);

		int nAnts = Integer.parseInt(args[0].toString());
		for (int i = 1; i <= nAnts; ++i)
		{
			AID aid = new AID(Global.SERVER, Global.getEjbName(Ant.class), "Ant" + i);
			agm.start(aid);
		}

		logger.fine("Starter done.");
	}

	@Override
	public void onMessage(ACLMessage message)
	{
	}
}
