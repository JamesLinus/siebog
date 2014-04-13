package org.xjaf2x.server.agents.acoalg;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.Clustered;
import org.xjaf2x.server.agentmanager.agent.AID;
import org.xjaf2x.server.agentmanager.agent.Agent;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import org.xjaf2x.server.messagemanager.fipaacl.Performative;

/**
 * Ant for Ant Colony Optimization
 * 
 * @author <a href="mailto:">Teodor</a>
 * @author <a href="mailto:milan.laketic@yahoo.com">Milan Laketic</a>
 */

@Stateful(name = "org_xjaf2x_server_agents_acoalg_Ant")
@Remote(AgentI.class)
@Clustered
public class Ant extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5124507266569403310L;
	
	private static final Logger logger = Logger.getLogger(Ant.class.getName());
	/**
	 * AID of the agent maintaining the world graph.
	 */
	private AID mapAID;

	/**
	 * Number of nodes on the map (provided by map agent).
	 */
	private int mapSize;

	/**
	 * Index of the current (x,y) pair in the map nodes list.
	 */
	private int currentMapPosIndex;

	/**
	 * Represents all nodes this ant has visited so far (in order of visit).
	 */
	private List<Integer> tourSoFar;

	/**
	 * I-th element represents the weight between i-th and (i+1)-th node in the
	 * tourSoFar list.
	 */
	private List<Float> tourSoFarWeights;

	/**
	 * Represents total weight of tourSoFar.
	 */
	private float totalWeightSoFar = 0f;
	

	

	/**
	 * Local pheromone influence control parameter.
	 * 
	 * 
	 */
	
	
	

	final float alpha = 1f;
	
	/**
	 * Edge weight influence control parameter.
	 */
	final float beta = 5f;
	
	/**
	 * Pheromone evaporation rate (0 <= ro < 1).
	 */
	final float ro = 0.1f;
	
	/**
	 * Pheromone local evaporation rate (0 <= ksi < 1).
	 */
	final float ksi = 0.1f;
	
	/**
	 * 1 - initial tour creation by probabilistic movement (send request)
	 * 2 - while waiting for a reply to the 'PheromoneLevels?' message (process paths)
	 * 3 - while waiting to obtain the weight of the last edge in the tour.
	 * 4 - preparing for backtrack: remove last node on the tourSoFar list (it is the same as the first)
	 *     and update the best tour (in the Map agent) if necessary.
	 * 5 - backtrack and pheromone adjustment
	 */
	
	
	/**
	 * Local pheromone change (see phase 4).
	 */
	float delta = 0; 
	
	
	
	/**
	 * Index of the map position entry this ant is currently at.
	 */
	//int currentMapPosIndex = 0;
	
	final Random rnd = new Random();
	
	
	
	
	/**
	 * Space-delimited potential node indices.
	 */
	//THIS VARIABLE WAS ORIGINALY IN CLASS BEHAVIOUR
	private String potentialNodeIndices ;
	
	private int phase =1;
	
	boolean tourDone=false;
	/**
	 * Agent initialization.
	 */
	@Override
	public void init(Serializable[] args) {

		

		try
		{		
			AID mapAidPattern = new AID("org_xjaf2x_server_agents_acoalg_Map", null);
			mapAID = agm.getRunning(mapAidPattern).get(0);
			
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "AgentManager initialization error", ex);
		}
		
		
		// choose starting map node randomly
		mapSize = initMapSize();
		

	}

	
	private int initMapSize()
	{

		logger.log(Level.FINE,"moja familija je: "+myAid.getFamily().toString()  );
		logger.log(Level.FINE,"postavljam velicinu mape"  );
		logger.log(Level.FINE,"aid mape je:"+ mapAID.toString()  );

		ACLMessage message = new ACLMessage();
		message.setPerformative(Performative.REQUEST);
		message.setContent("MapSize?");
		message.setSender(myAid);


		
		logger.log(Level.FINE,"saljem poruku na mapaid:"+mapAID.toString()  );
		logger.log(Level.FINE,"moj aid je: "+myAid.toString()  );
		message.addReceiver(mapAID);
		msm.post(message);
		logger.log(Level.FINE,"poslao sam poruku"  );

		
		return 0;
	}
	
	@Override
	public void onMessage(ACLMessage message) {
		// TODO Auto-generated method stub
		
		String reply = (String)message.getReplyWith();
		

		logger.log(Level.FINE,"mravu je stigla poruka koja ima reply with "+reply  );
		
		if(message.getReplyWith().equals("MapSize")){

				logger.log(Level.FINE,"stigla mi je velicina mape i ona iznosi: "+message.getContent()  );
				String cont = (String) message.getContent();
				mapSize =  Integer.parseInt(cont);
				currentMapPosIndex = new Random().nextInt(mapSize);

				tourSoFar = new ArrayList<Integer>();
				tourSoFar.add(currentMapPosIndex);
				
				tourSoFarWeights = new ArrayList<Float>();
				reply="phase1";
			}
		
		
		
			
		switch (reply)
		{
			case "phase1":
			{

				logger.log(Level.FINE, myAid.toString()+ " ant enters phase 1." );
				// choose next node (send request)
				if(!tourDone){
				ACLMessage request = new ACLMessage(Performative.REQUEST);
				potentialNodeIndices = GetPotentialNodeIndices().trim();

				currentMapPosIndex = GetCurrentMapPosIndex();

				request.setContent("PheromoneLevels? " + currentMapPosIndex + " " + potentialNodeIndices);
				request.addReceiver(mapAID);
				request.setSender(myAid);
				request.setReplyWith("PheromoneLevels");
				msm.post(request);
				
				phase = 2;
				}else{
					System.out.println("tour exceded 51");
				}
				break;
			}
			case "PheromoneLevels":
			{


				
				logger.log(Level.FINE, myAid.toString()+ " ant enters phase 2." );
				// choose next node (response received)
				int newNodeIndex = 0;



				
					// response contains a header and alternating numbers designating pheromone level and edge weight for every applicable edge
					String cont = (String)message.getContent();
					String[] parts = cont.split(" ");

					// set pheromone and weight hashmaps
					java.util.Map<Integer, Float> pheromones = new HashMap<Integer, Float>();
					java.util.Map<Integer, Float> weights = new HashMap<Integer, Float>();
					float total = 0f;
					for (int i = 1; i < parts.length; i += 2)
					{
						float weight = Float.parseFloat(parts[i + 1]);
						float pheromoneLevel = Float.parseFloat(parts[i]);
						float val = (float)(Math.pow(pheromoneLevel, alpha) * Math.pow(1 / weight, beta));
						total += val;
						pheromones.put((i + 1) / 2, val);
						weights.put((i + 1) / 2, weight);
					}
					
					// set probability distribution
					java.util.Map<Integer, Float> probabilities = new HashMap<Integer, Float>();
					for (int i = 1; i <= pheromones.size(); ++i)
						probabilities.put(i, pheromones.get(i) / total);
					
					// choose next pheromone index using probability distribution
					double random = rnd.nextDouble();
					int i = 1;
					float val = probabilities.get(i);
					while (i < probabilities.size())
					{
						if (random < val)
							break;
						else
							val += probabilities.get(++i);
					}
					
					// update next node index (using chosen pheromone index i, and potentialNodeIndices string)
					

					
					String[] segs = potentialNodeIndices.split(" ");
					newNodeIndex = Integer.parseInt(segs[i - 1]);
					SetCurrentMapPosIndex(newNodeIndex);
					
					// add new node to tourSoFar
					AddNodeToTour(newNodeIndex);
					
					// add new weight to tourSoFarWeights
					AddWeightToTour(weights.get(i));
					
					// initiate local pheromone update
					ACLMessage localUpdate = new ACLMessage(Performative.INFORM);
					localUpdate.addReceiver(mapAID);
					localUpdate.setContent("UpdateLocalPheromone " + currentMapPosIndex + " " + newNodeIndex + " " + ksi);
					
					// advance the phase as required (if tour complete, continue with phase 3, otherwise, repeat phase 1)
	
					logger.log(Level.FINE, "getTourSoFar mi je: "+GetTourSoFarSize()+" getMapSize mi je: "+GetMapSize() );
					if (GetTourSoFarSize() == GetMapSize())
					{
						int firstMapPosIndex = GetFirstMapPosIndex();
						AddNodeToTour(firstMapPosIndex);
						
						ACLMessage edgeWeightReq = new ACLMessage(Performative.REQUEST);
						edgeWeightReq.addReceiver(mapAID);
						edgeWeightReq.setContent("EdgeWeight? " + currentMapPosIndex + " " + firstMapPosIndex);
						edgeWeightReq.setSender(myAid);
						msm.post(edgeWeightReq);
						
						currentMapPosIndex = firstMapPosIndex;
						SetCurrentMapPosIndex(firstMapPosIndex);
						
						phase = 3;

						logger.log(Level.FINE, "faza podesena na 3" );
						tourDone=true;
					}
					else{
						phase = 1;							
						ACLMessage porukaSamomSebi = new ACLMessage();
						porukaSamomSebi.addReceiver(myAid);
						porukaSamomSebi.setReplyWith("phase1");
						msm.post(porukaSamomSebi);	
					}
					
				
				break;
			}
			case "EdgeWeight":
			{

					logger.log(Level.FINE, myAid.toString()+ " enters phase 3.");

				
					String cont = (String)message.getContent();

					logger.log(Level.FINE, "sadrzaj u case 3 mi je: "+cont);
					AddWeightToTour(Float.parseFloat(cont));
					
					phase = 4;
					ACLMessage porukaSamomSebi = new ACLMessage();
					porukaSamomSebi.setReplyWith("case4");
					porukaSamomSebi.addReceiver(myAid);
					msm.post(porukaSamomSebi);
				
								
				break;
				
			}
			case "case4":
			{
				logger.log(Level.FINE, myAid.toString()+" enters phase 4.");
				
				ACLMessage updateBest = new ACLMessage(Performative.INFORM);
				updateBest.addReceiver(mapAID);


				StringBuilder tourSoFar = new StringBuilder();
				for (int i = 0; i < GetTourSoFarSize(); ++i)
					tourSoFar.append(" ").append(GetTourNode(i));
				float tourWeight = GetTotalWeightSoFar();
				updateBest.setContent("UpdateBestTour " + tourWeight + tourSoFar.toString());
				updateBest.setSender(myAid);
				updateBest.setReplyWith("updateBestTour");
				

				msm.post(updateBest);
				
				delta = 1 / tourWeight;
				
				RemoveLastNode(); // which is the same as the first
				
				phase = 5;
				ACLMessage porukaSamomSebi = new ACLMessage();
				porukaSamomSebi.setReplyWith("default");
				porukaSamomSebi.addReceiver(myAid);
				msm.post(porukaSamomSebi);
				break;
			}
			case "phase6":
			{
				terminate();
				logger.log(Level.FINE, myAid.toString()+" ubiven");
			}
			case "default": // phase == 5
			{

				logger.log(Level.FINE, myAid.toString()+" enters phase 5.");
				// backtrack and pheromone adjustment
				int nextNodeIndex = RemoveLastNode();

				logger.log(Level.FINE, "next node index je: "+nextNodeIndex);
				if (nextNodeIndex == -1)
				{
					phase = 6;

					
					// when this ant is done, create another one
					agm.startAgent("org_xjaf2x_server_agents_acoalg_Ant", ""+System.currentTimeMillis(), null);

					return;
				}
				

				
				currentMapPosIndex = GetCurrentMapPosIndex();
				
				ACLMessage updatePheromone = new ACLMessage(Performative.INFORM);
				updatePheromone.addReceiver(mapAID);
				//float val = (1 - ro) * oldValue + ro * delta; // final formula is constructed in Map agent (for simplicity of oldValue retrieval)
				updatePheromone.setContent("UpdatePheromone " + currentMapPosIndex + " " + nextNodeIndex + " " + (1 - ro) + " " + ro * delta);
				updatePheromone.setSender(myAid);
				msm.post(updatePheromone);
				
				SetCurrentMapPosIndex(nextNodeIndex);
				
				
				ACLMessage probamoOpetDefault = new ACLMessage();
				probamoOpetDefault.setReplyWith("default");
				probamoOpetDefault.addReceiver(myAid);
				msm.post(probamoOpetDefault);
				
				if(phase==6){
					ACLMessage killMe = new ACLMessage();
					killMe.addReceiver(myAid);
					killMe.setReplyWith("phase6");
					msm.post(killMe);
					logger.log(Level.FINE, "sending death message");
				}
				
				
				break;
			}
			
			
		}
		
		
	}
	
	/**
	 * @return size of the tour so far.
	 */
	public int GetTourSoFarSize()
	{
		return tourSoFar.size();
	}
	
	/**
	 * @return number of nodes on the map.
	 */
	public int GetMapSize()
	{
		return mapSize;
	}
	
	/**
	 * Adds 'nodeIndex' to the 'tourSoFar' list.
	 */
	public void AddNodeToTour(int nodeIndex)
	{
		tourSoFar.add(nodeIndex);
	}
	
	public void AddWeightToTour(float weight)
	{
		tourSoFarWeights.add(weight);
		totalWeightSoFar += weight;
	}
	
	/**
	 * @return space-delimited indices of unvisited nodes.
	 */
	public String GetPotentialNodeIndices()
	{
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < mapSize; ++i)
			if (!tourSoFar.contains(i))
				result.append(" ").append(i);
		
		
		return result.toString();
	}
	
	/**
	 * @return index of the map node (in map's 'nodes' list) this ant is currently at.
	 */
	public int GetCurrentMapPosIndex()
	{
		return currentMapPosIndex;
	}
	
	/**
	 * Set value as the current map position index.
	 */
	public void SetCurrentMapPosIndex(int value)
	{
		currentMapPosIndex = value;
	}
	
	/**
	 * @return index of the first node that was visited on the current tour.
	 */
	public int GetFirstMapPosIndex()
	{
		return tourSoFar.get(0);
	}
	
	/**
	 * @return total weight of the tourSoFar.
	 */
	public float GetTotalWeightSoFar()
	{
		return totalWeightSoFar;
	}
	
	/**
	 * @return Last node of the tourSoFar list, which is subsequently removed,
	 * or -1, if the list is already empty.
	 */
	public int RemoveLastNode()
	{

		if (tourSoFar.size() != 0)
			return tourSoFar.remove(tourSoFar.size() - 1);
		else
			return -1;
	}
	
	/**
	 * @return Last edge weight of the tourSoFarWeights list, which is subsequently removed,
	 * or -1, if the list is already empty;
	 */
	public float RemoveLastWeight()
	{
		if (tourSoFarWeights.size() != 0)
			return tourSoFarWeights.remove(tourSoFarWeights.size() - 1);
		else
			return -1f;
	}
	
	/**
	 * @return tourSoFar element with index 'index'.
	 */
	public int GetTourNode(int index)
	{
		return tourSoFar.get(index);
	}
	
	/**
	 * Agent clean-up.
	 */
	@Override
	@Remove
	public void terminate()
	{
	//	System.out.println("Ant terminated.");
	}

}
