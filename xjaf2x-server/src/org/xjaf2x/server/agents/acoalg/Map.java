package org.xjaf2x.server.agents.acoalg;





import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.Clustered;
import org.xjaf2x.server.agentmanager.agent.Agent;
import org.xjaf2x.server.agentmanager.agent.AgentI;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import org.xjaf2x.server.messagemanager.fipaacl.Performative;



/**
 * Map for Ant Colony Optimization
 * 
 * @author <a href="mailto:">Teodor</a>
 * @author <a href="mailto:milan.laketic@yahoo.com">Milan Laketic</a>
 */


@Stateful(name = "org_xjaf2x_server_agents_acoalg_Map")
@Remote(AgentI.class)
@Clustered
public class Map extends Agent {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = 4998652517108886246L;

	private static final Logger logger = Logger.getLogger(Map.class.getName());
	//using messagemanager for sending messages
	//MessageManagerI messageManager ;

	
	/**
	 * TSP graph (given by a set of (x,y)-points).
	 */
	private List<Node> nodes;
	
	/**
	 * Initial pheromone value.
	 */
	private float tau0;
	
	/**
	 * Pheromone values for individual graph edges.
	 */
	private float[][] pheromone;
	
	/**
	 * Weight of the best tour found so far.
	 */
	 float bestTourWeight = Float.MAX_VALUE;
		/**
		 * Represents a heuristic for program termination.
		 * When this variable reaches MAX_STATIONARY_ITERATIONS,
		 * the program terminates.
		 */
		 int nIterationsBestTourNotUpdated = 0;
		
		/**
		 * Heuristic boundary value for program termination.
		 */
		 final int MAX_STATIONARY_ITERATIONS = 500;
	 
	 
	 
	/**
	 * Represents a single graph vertex.
	 */
	class Node
	{
		private float x;
		private float y;
		
		public Node(float x, float y)
		{
			this.x = x;
			this.y = y;
		}
		
		public float getX()
		{
			return x;
		}
		
		public float getY()
		{
			return y;
		}
		
		public void setX(float x)
		{
			this.x = x;
		}
		
		public void setY(float y)
		{
			this.y = y;
		}
		
		@Override
		public String toString()
		{
			return "(" + x + ", " + y + ")";
		}
	}
	
	@Override
	public void init(Serializable[] args)
	{
		System.out.println("Map opened.");
		
		//loadMap("eil51.tsp");   //best result: 463.23953 (optimal: 426)         xjaf2x best result 458.79224
		//loadMap("eil76.tsp");   //best result: 586.95886 (optimal: 538)
		//loadMap("eil101.tsp");  //best result: 745.56305 (optimal: 629)           xjaf2x best result 750.75714
		//loadMap("ch130.tsp");   //best result: 7094.9155 (optimal: 6110)			xjaf2x best result 7358.2905
		loadMap("ch150.tsp");   //best result: 7515.092  (optimal: 6528)			xjaf2x best result 7387.147
		

	}
	
	

	//in jade cyclic behaviour is extended, but here onMessage acts like cyclic because it will always respond on message
	@Override
	public void onMessage(ACLMessage message) {
		

		
		

		/**
		 * Best tour found so far.
		 */
		 List<Integer> bestTour;
		

		 
		 if(message != null)
		 {

			 logger.log(Level.FINE, "mapi je stigla poruka koja ima reply with "+message.getReplyWith() );
			 String cont = (String) message.getContent();;
			 
			 ACLMessage reply = message.makeReply(Performative.INFORM);
			  
			 if(message.getPerformative()== Performative.REQUEST)
			 
			 {
			
				 if(cont.equals("MapSize?"))
				 {
					 reply.setReplyWith("MapSize");
					 reply.setContent(String.valueOf(GetMapSize()));
					
				 }
				 else if (cont.startsWith("PheromoneLevels?"))
				 {
					 String[] parts = cont.split(" ");
						StringBuilder pheromoneLevels = new StringBuilder();
						
						int i = Integer.parseInt(parts[1]);
						pheromoneLevels.append("PheromoneLevels:");
						for (int j = 2; j < parts.length; ++j)
						{
							int newJ = Integer.parseInt(parts[j]);
							pheromoneLevels.append(" ")
								.append(GetPheromoneLevel(i, newJ))
								.append(" ")
								.append(GetEdgeWeight(i, newJ));
						}
						reply.setContent(pheromoneLevels.toString());
						
						reply.setReplyWith("PheromoneLevels");

						logger.log(Level.FINE, "pakujem nivo feromona u poruku");
					
				 }
				 else if (cont.startsWith("EdgeWeight?"))
				{
						String[] parts = cont.split(" ");
						reply.setContent(String.valueOf(GetEdgeWeight(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]))));
						reply.setReplyWith("EdgeWeight");
		
						logger.log(Level.FINE, "stavio sam edgeweight u poruku");
				}
				 msm.post(reply);

				 logger.log(Level.FINE, "poslao sam odgovor");
			 }
			 else if (message.getPerformative() == Performative.INFORM){



				 if (cont.startsWith("UpdateBestTour"))	 
				 {
					 
					 String[] parts = cont.split(" ");
					float newTourWeight = Float.parseFloat(parts[1]);
					nIterationsBestTourNotUpdated++;

					logger.log(Level.FINE, "newTourWeight je : "+newTourWeight+"a best je:"+bestTourWeight);
					if (bestTourWeight > newTourWeight)
					{
						nIterationsBestTourNotUpdated = 0;
						
						bestTourWeight = newTourWeight;
						
						bestTour = new ArrayList<Integer>();
						for (int i = 2; i < parts.length; ++i)
							bestTour.add(Integer.parseInt(parts[i]) + 1);
						
						System.out.println("Best tour so far has weight: " + bestTourWeight);
						System.out.println("Best tour so far: " + bestTour);
					}
					if (nIterationsBestTourNotUpdated == MAX_STATIONARY_ITERATIONS)
					{
						
						System.exit(0);
					}
				 }
				 else if (cont.startsWith("UpdatePheromone"))
				 {
						String[] parts = cont.split(" ");
						int i = Integer.parseInt(parts[1]);
						int j = Integer.parseInt(parts[2]);
						SetPheromoneLevel(i, j, Float.parseFloat(parts[3]) * GetPheromoneLevel(i, j) + Float.parseFloat(parts[4]));
				}
				else if (cont.startsWith("UpdateLocalPheromone"))
				{
						String[] parts = cont.split(" ");
						int i = Integer.parseInt(parts[1]);
						int j = Integer.parseInt(parts[2]);
						float ksi = Float.parseFloat(parts[3]);
						SetPheromoneLevel(i, j, (1 - ksi) * GetPheromoneLevel(i, j) + ksi * tau0);
				}
			 }
		 }
			 else
				 	//There is no block() method in here but message is never null!
				 	System.out.println("treba da se blokiram");

				
			 
			 
		 }
		
	

	
	private void loadMap(String fileName)
	{
		nodes = new ArrayList<Node>();
		
		BufferedReader reader = null;
		try
		{
			
			reader = new BufferedReader(new InputStreamReader(Map.class.getResourceAsStream("/"+fileName)));
			
			// skip preliminary info
			for (int i = 0; i < 6; ++i)
				reader.readLine();
			
			// load the map
			String line = null;
			String[] parts = null;
			while (!(line = reader.readLine()).equals("EOF"))
			{
				parts = line.split(" ");
				nodes.add(new Node(Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
			}
			
			// print the map
			System.out.print("Map: ");
			for (Node n : nodes)
				System.out.print(n + " ");
			System.out.println();
			
			// initialize pheromone levels
			int n = nodes.size();
			float C = n * getAverageWeight();
			tau0 = 1 / (n * C);
			pheromone = new float[n][n];
			for (int i = 0; i < n; ++i)
				for (int j = 0; j < n; ++j)
					pheromone[i][j] = tau0;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			if (reader != null)
				try
				{
					reader.close();
				}
				catch (Exception ex2)
				{
					ex2.printStackTrace();
				}
		}
	}
	
	/**
	 * @return average edge weight (Euclid2D) of the currently loaded map.
	 */
	private float getAverageWeight()
	{
		float result = 0f;
		int n = nodes.size();
		for (int i = 0; i < n; ++i)
			for (int j = 0; j < n; ++j)
				result += GetEdgeWeight(i, j);
		return result / (n * n);
	}
	

	
	/**
	 * @return the number of nodes on the map.
	 */
	public int GetMapSize()
	{
		return nodes.size();
	}
	
	/**
	 * @return current pheromone level of the edge between nodes i and j. 
	 */
	public float GetPheromoneLevel(int i, int j)
	{
		return pheromone[i][j];
	}
	
	/**
	 * Set pheromone level of (i,j)-edge to 'val'.
	 */
	public void SetPheromoneLevel(int i, int j, float val)
	{
		pheromone[i][j] = val;
	}
	
	/**
	 * @return weight of the (i,j) edge
	 * (Euclid2D distance between node i and node j).
	 */
	public float GetEdgeWeight(int i, int j)
	{
		Node ni = nodes.get(i);
		Node nj = nodes.get(j);
		return (float)(Math.sqrt(Math.pow(ni.getX() - nj.getX(), 2) + Math.pow(ni.getY() - nj.getY(), 2)));
	}
	
	/**
	 * Agent clean-up.
	 * anotation @Remove terminates agent
	 */
	@Override
	@Remove
	public void terminate()
	{

		logger.info("Map closed.");
		
	}

}
