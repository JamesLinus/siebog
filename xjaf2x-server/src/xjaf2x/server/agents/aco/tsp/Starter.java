package xjaf2x.server.agents.aco.tsp;

import java.io.Serializable;
import java.util.logging.Logger;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import xjaf2x.server.agentmanager.agent.Agent;
import xjaf2x.server.agentmanager.agent.AgentI;
import xjaf2x.server.messagemanager.fipaacl.ACLMessage;

/**
 * Starter agent, entry point.
 * 
 * @author <a href="mailto:tntvteod@neobee.net">Teodor Najdan Trifunov</a>
 * @author <a href="mailto:milan.laketic@yahoo.com">Milan Laketic</a>
 */
@Stateless(name = "xjaf2x_server_agents_aco_tsp_Starter")
@Remote(AgentI.class)
public class Starter extends Agent
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Starter.class.getName());

	@Override
	public void init(Serializable... args)
	{
		logger.fine("Starter agent running.");

		agm.startAgent("xjaf2x_server_agents_aco_tsp_Map", "Map", args[1]);

		int nAnts = Integer.parseInt(args[0].toString());
		for (int i = 1; i <= nAnts; ++i)
			agm.startAgent("xjaf2x_server_agents_aco_tsp_Ant", "Ant" + i);

		logger.fine("Starter done.");
	}

	@Override
	public void onMessage(ACLMessage message)
	{
	}
}
