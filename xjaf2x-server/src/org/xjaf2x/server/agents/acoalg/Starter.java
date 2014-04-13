package org.xjaf2x.server.agents.acoalg;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.xjaf2x.server.agentmanager.agent.AID;
import org.xjaf2x.server.agentmanager.agent.Agent;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;

/**
 * Starter agent Ant Colony Optimization
 * 
 * @author <a href="mailto:">Teodor</a>
 * @author <a href="mailto:milan.laketic@yahoo.com">Milan Laketic</a>
 */


@Stateful(name = "org_xjaf2x_server_agents_acoalg_Starter")
@Remote(AgentI.class)
public class Starter extends Agent {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7479115160359450756L;

	private static final Logger logger = Logger.getLogger(Starter.class.getName());

	/**
	 * Number of ants to create.
	 */
	private static final int nAnts = 10;
	
	@Override
	public void onMessage(ACLMessage message) {
		// TODO Auto-generated method stub
		System.out.println("Starter agent running.");
		
	}
	@SuppressWarnings("unused")
	@Override
	public void init(Serializable[] args){
		
		

		//Create Map
		try
		{

			AID mapAID = agm.startAgent("org_xjaf2x_server_agents_acoalg_Map", "Map", null);
			
		} catch (Exception ex)
		{
			if (logger.isLoggable(Level.INFO))
				logger.log(Level.INFO, "Error while performing a lookup of [ Map ]", ex);
		//	acoAgents.remove(aidMap.getFamily());
			
		}
		//Create Ants
		
		for (int i = 1; i < nAnts; ++i){

			try
			{
				AID antAID = agm.startAgent("org_xjaf2x_server_agents_acoalg_Ant", "Ant"+i, null);
				
			} catch (Exception ex)
			{
				if (logger.isLoggable(Level.INFO))
					logger.log(Level.INFO, "Error while performing a lookup of [ Ant"+i+" ]", ex);

				
			}
			
		}
		
		
	}
	
	@Override
	@Remove
	public	void terminate(){
		System.out.println("Starter terminated");
	}
	
	

}
